package fieldservice;

import java.net.http.HttpClient;
import java.util.List;

public final class FieldServiceJobDemo {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        FieldServiceJob job = new FieldServiceJob("WO-1042", "FAILED", List.of(), false);
        FollowUpDecision result = new JobFollowUpService(
                new InfraiErrorsClient(HttpClient.newHttpClient(), key)).inspect(job);
        System.out.println(result.followUpRequired() + ": " + result.reason());
    }
}
