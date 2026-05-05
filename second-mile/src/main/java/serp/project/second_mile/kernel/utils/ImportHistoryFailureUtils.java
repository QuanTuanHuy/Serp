/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.second_mile.enums.ImportHistoryStatus;
import serp.project.second_mile.repository.ImportHistoryRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ImportHistoryFailureUtils {

    private final ImportHistoryRepository importHistoryRepository;

    public void markImportFailed(Long importHistoryId, Long tenantId, String errorMessage, int maxErrorMessageLength) {
        importHistoryRepository.findByIdAndTenantId(importHistoryId, tenantId).ifPresent(importHistory -> {
            importHistory.setStatus(ImportHistoryStatus.FAILED);
            if (importHistory.getStartedAt() == null) {
                importHistory.setStartedAt(LocalDateTime.now());
            }
            if (importHistory.getTotalRecords() == null) {
                importHistory.setTotalRecords(0);
            }
            if (importHistory.getSuccessRecords() == null) {
                importHistory.setSuccessRecords(0);
            }
            if (importHistory.getFailedRecords() == null) {
                importHistory.setFailedRecords(importHistory.getTotalRecords());
            }
            importHistory.setErrorMessage(ImportErrorUtils.truncateErrorMessage(errorMessage, maxErrorMessageLength));
            importHistory.setFinishedAt(LocalDateTime.now());
            importHistoryRepository.save(importHistory);
        });
    }
}
