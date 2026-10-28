package fieldservice;

import java.util.List;

public record FieldServiceJob(String workOrderId, String dispatchStatus, List<String> photoIds,
                              boolean technicianFollowUpRecorded) { }
