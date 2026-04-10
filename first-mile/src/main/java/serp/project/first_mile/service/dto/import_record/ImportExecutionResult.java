package serp.project.first_mile.service.dto.import_record;

public record ImportExecutionResult(
        int totalRecords,
        int successRecords,
        int failedRecords,
        String errorMessage
) {
}