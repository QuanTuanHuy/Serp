package serp.project.tms_order.service.dto.import_record;

public record ImportExecutionResult(
        int totalRecords,
        int successRecords,
        int failedRecords,
        String errorMessage
) {
}
