package fieldservice;

import java.io.IOException;
import java.util.UUID;

public final class JobFollowUpService {
    private final ErrorReporter errors;

    public JobFollowUpService(ErrorReporter errors) {
        this.errors = errors;
    }

    public FollowUpDecision inspect(FieldServiceJob job) throws IOException, InterruptedException {
        if (job.dispatchStatus().equals("FAILED") && job.photoIds().isEmpty()
                && !job.technicianFollowUpRecorded()) {
            String payload = "work_order=" + job.workOrderId() + "; status=FAILED; photos=missing; follow_up=missing";
            errors.capture(payload, "field-service-" + UUID.nameUUIDFromBytes(job.workOrderId().getBytes()));
            return new FollowUpDecision(true, "failed dispatch needs technician follow-up");
        }
        return new FollowUpDecision(false, "no follow-up signal");
    }
}
