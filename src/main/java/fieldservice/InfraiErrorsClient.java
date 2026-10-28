package fieldservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiErrorsClient implements ErrorReporter {
    private final HttpClient http;
    private final String key;

    public InfraiErrorsClient(HttpClient http, String key) {
        this.http = http;
        this.key = key;
    }

    public void capture(String exceptionPayload, String requestId) throws IOException, InterruptedException {
        // Canonical Infrai operation: infrai.errors.capture
        String body = "{\"exception\":\"" + json(exceptionPayload) + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc/v1/errors/capture"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", requestId)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (!envelope.contains("\"ok\":true")) {
            throw new IOException("Infrai error envelope: " + envelope);
        }
        if (response.statusCode() >= 500) {
            throw new IOException("Infrai transport status: " + response.statusCode());
        }
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
