package nl.utwente.apollo.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.utwente.apollo.Apollo;
import nl.utwente.apollo.JsonStorage;
import nl.utwente.apollo.pmd.ApolloPMDRunner;
import nl.utwente.atelier.api.AtelierAPI;
import nl.utwente.atelier.exceptions.CryptoException;
import nl.utwente.processing.ProcessingFile;
import nl.utwente.processing.ProcessingProject;
import nl.utwente.processing.pmd.PMDException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/** Handler for Webhook requests. It checks if the request is valid and handles supported events. */
public class WebhookHandler {
    private final String webhookSecret;
    private final AtelierAPI api;
    private final JsonStorage storage;
    private final ApolloPMDRunner pmd = new ApolloPMDRunner();

    public WebhookHandler(Configuration config, AtelierAPI api, JsonStorage storage) {
        this.webhookSecret = config.getWebhookSecret();
        this.api = api;
        this.storage = storage;
    }

    /** Indicates that the request is not valid due to the given reason */
    private class InvalidWebhookRequest extends Throwable {
        public InvalidWebhookRequest(String reason) {
            super("Invalid request. " + reason);
        }
    }

    /** Check that the request has the correct User-Agent header */
    private void checkUserAgent(HttpServletRequest request) throws InvalidWebhookRequest {
        var userAgent = request.getHeader("User-Agent");
        if (userAgent == null || !userAgent.equals("Atelier")) {
            throw new InvalidWebhookRequest("Unknown User-Agent.");
        }
    }

    /** Check that the request has the correct signature, corresponding to the WebhookSecret */
    private void checkSignature(HttpServletRequest request, byte[] body) throws InvalidWebhookRequest, CryptoException {
        var signature = request.getHeader("X-Atelier-Signature");
        if (signature == null) {
            throw new InvalidWebhookRequest("No signature provided.");
        }

        try {
            var secret = webhookSecret.getBytes();
            var key = new SecretKeySpec(secret, "HmacSHA1");
            var hmac = Mac.getInstance("HmacSHA1");
            hmac.init(key);

            var rawSignature = hmac.doFinal(body);
            var computedSignature = Base64.getEncoder().encodeToString(rawSignature);

            if (!computedSignature.equals(signature)) {
                throw new InvalidWebhookRequest("Invalid signature.");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    /** Handle an incoming Webhook request */
    public void handleWebhook(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Received new webhook request.");
        try {
            // I'm sorry.
            // The inner catch handler may throw an IOException, which should be handled in
            // the same way as any thrown in try-block. The alternative would be another
            // try-catch inside the catch, with duplicated error-handling code.
            try {
                checkUserAgent(request);
                var rawBody = request.getInputStream().readAllBytes();
                checkSignature(request, rawBody);
                var body = new String(rawBody, "UTF-8");
                var json = JsonParser.parseString(body);
                var event = json.getAsJsonObject().get("event").getAsString();
                var payload = json.getAsJsonObject().get("payload").getAsJsonObject();
                switch (event) {
                    case "submission":
                        handleSubmission(payload);
                        break;
                }
                response.setStatus(200);
            } catch (InvalidWebhookRequest e) {
                System.out.println(e.getMessage());
                response.setStatus(400);
                var writer = response.getWriter();
                writer.println(e.getMessage());
                writer.flush();
                writer.close();
            }
        } catch (IOException | CryptoException | PMDException e) {
            response.setStatus(500);
            e.printStackTrace();
        }
    }

    /** Handle events of type 'submission' */
    private void handleSubmission(JsonObject submission) throws IOException, CryptoException, PMDException {
        var submissionID = submission.get("ID").getAsString();
        System.out.printf("Handling submission %s%n", submissionID);
        try {
            var files = StreamSupport.stream(submission.get("files").getAsJsonArray().spliterator(), true)
                .map(file -> file.getAsJsonObject())
                .filter(file -> file.get("name").getAsString().endsWith(".pde"))
                .map(file -> {
                    try {
                        var fileName = file.get("name").getAsString();
                        var fileID = file.get("ID").getAsString();
                        var res = api.getFile(fileID);
                        if (res.getStatusLine().getStatusCode() < 400) {
                            var fileContent = new String(res.getEntity().getContent().readAllBytes());
                            return new ProcessingFile(fileID, fileName, fileContent);
                        } else {
                            var message = String.format("Request for file %s returned status %d.", fileID, res.getStatusLine().getStatusCode());
                            System.out.printf(message);
                            throw new IOException(message);
                        }
                    } catch (IOException | CryptoException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toList());
            var project = new ProcessingProject(files);

            var metrics = pmd.run(project);

            storage.storeSubmissionResults(submissionID, metrics);

            var message =
                    Apollo.formatResults(metrics, false)
                    + "\nPlease reply with +1 if you think this is correct, or -1 if you don't. If you have time, please explain why!";

            var comment = new JsonObject();

            comment.addProperty("submissionID", submissionID);
            comment.addProperty("visibility", "private");
            comment.addProperty("comment", message);

            var res = api.postProjectComment(submissionID, comment);
            if (res.getStatusLine().getStatusCode() == 200) {
                var resJson = JsonParser.parseReader(new InputStreamReader(res.getEntity().getContent()));
                var threadID = resJson.getAsJsonObject().get("ID").getAsString();
                System.out.println("Made comment " + threadID + " for Apollo results");
            } else {
                System.out.println("Request to make comment failed. Got status " + res.getStatusLine().getStatusCode());
            }
        } catch (RuntimeException ex) {
            var cause = ex.getCause();
            if (cause instanceof IOException)
                throw (IOException) cause;
            else if (cause instanceof CryptoException)
                throw (CryptoException) cause;
            else if (cause instanceof PMDException)
                throw (PMDException) cause;
            else
                throw ex;
        }
    }
}