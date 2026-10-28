package fieldservice;

import java.io.IOException;
import java.util.List;

public final class JobFollowUpServiceTest {
    public static void main(String[] args) throws Exception {
        RecordingReporter reporter = new RecordingReporter();
        FieldServiceJob failed = new FieldServiceJob("WO-test", "FAILED", List.of(), false);
        FollowUpDecision decision = new JobFollowUpService(reporter).inspect(failed);
        if (!decision.followUpRequired()) throw new AssertionError("failed job must require follow-up");
        if (!decision.reason().contains("technician")) throw new AssertionError("reason must name technician follow-up");
        if (!reporter.payload.contains("WO-test")) throw new AssertionError("failed work order must be reported");
        FieldServiceJob complete = new FieldServiceJob("WO-complete", "DISPATCHED", List.of("photo-1"), true);
        if (new JobFollowUpService(reporter).inspect(complete).followUpRequired()) {
            throw new AssertionError("complete job must not require follow-up");
        }
        System.out.println("JobFollowUpServiceTest passed");
    }

    private static final class RecordingReporter implements ErrorReporter {
        private String payload = "";

        @Override
        public void capture(String exceptionPayload, String requestId) throws IOException {
            payload = exceptionPayload;
        }
    }
}
