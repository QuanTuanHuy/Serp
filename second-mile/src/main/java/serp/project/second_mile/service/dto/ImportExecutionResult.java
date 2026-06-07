/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.dto;

public record ImportExecutionResult(
        int totalRecords,
        int successRecords,
        int failedRecords,
        String errorMessage
) {
}
