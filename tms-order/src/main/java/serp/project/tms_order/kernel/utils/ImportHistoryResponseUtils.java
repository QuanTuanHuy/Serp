/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kernel.utils;

import serp.project.tms_order.domain.ImportHistory;
import serp.project.tms_order.dto.response.ImportHistoryResponse;

public final class ImportHistoryResponseUtils {

    private ImportHistoryResponseUtils() {
    }

    public static ImportHistoryResponse toResponse(ImportHistory importHistory) {
        return toResponse(importHistory, false);
    }

    public static ImportHistoryResponse toResponse(ImportHistory importHistory, boolean includeType) {
        ImportHistoryResponse.ImportHistoryResponseBuilder builder = ImportHistoryResponse.builder()
                .id(importHistory.getId())
                .fileId(importHistory.getFileId())
                .fileName(importHistory.getFileName())
                .status(importHistory.getStatus())
                .totalRecords(importHistory.getTotalRecords())
                .successRecords(importHistory.getSuccessRecords())
                .failedRecords(importHistory.getFailedRecords())
                .errorMessage(importHistory.getErrorMessage())
                .startedAt(importHistory.getStartedAt())
                .finishedAt(importHistory.getFinishedAt());

        if (includeType) {
            builder.type(importHistory.getType());
        }

        return builder.build();
    }
}
