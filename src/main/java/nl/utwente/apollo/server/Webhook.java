package nl.utwente.apollo.server;

import nl.utwente.apollo.JsonStorage;
import nl.utwente.atelier.api.AtelierAPI;
import nl.utwente.atelier.api.Authentication;
import nl.utwente.atelier.exceptions.ConfigurationException;
import nl.utwente.atelier.exceptions.CryptoException;
import org.apache.http.impl.client.HttpClients;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

/** Servlet for Webhook requests, entrypoint of the application */
public class Webhook extends HttpServlet {
    private WebhookHandler handler;

    public Webhook() throws IOException, CryptoException, ConfigurationException, URISyntaxException {
        var httpClient = HttpClients.createDefault();
        var config = Configuration.readFromFile();
        var auth = new Authentication(config, httpClient);
        var api = new AtelierAPI(config, auth, httpClient);
        var storage = new JsonStorage(Path.of(config.getStorageLocation()));
        this.handler = new WebhookHandler(config, api, storage);
        System.out.println("Webhook started.");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handler.handleWebhook(request, response);
    }
}