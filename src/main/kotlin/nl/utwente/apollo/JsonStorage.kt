package nl.utwente.apollo

import com.google.gson.JsonObject
import java.nio.file.Files
import java.nio.file.Path

class JsonStorage(val storagePath: Path) {

    fun storeSubmissionResults(submissionId: String, metrics: Map<Metrics, Double>) {
        val json = JsonObject()
        json.addProperty("submissionId", submissionId)
        for ((metric, value) in metrics)
            json.addProperty(metric.name, value)

        val file = storagePath.resolve("$submissionId.json")
        Files.writeString(file, json.toString())
    }
}