package com.example.ttcrs.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LocationImportResult {

    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<LocationImportError> errors;
    private List<LocationResponseDTO> createdLocations;

    public static LocationImportResult success(List<LocationResponseDTO> created, int totalRows) {
        return LocationImportResult.builder()
                .totalRows(totalRows)
                .successCount(created.size())
                .errorCount(0)
                .errors(List.of())
                .createdLocations(created)
                .build();
    }

    public static LocationImportResult partial(
            int totalRows, List<LocationResponseDTO> created, List<LocationImportError> errors) {
        return LocationImportResult.builder()
                .totalRows(totalRows)
                .successCount(created.size())
                .errorCount(errors.size())
                .errors(errors)
                .createdLocations(created)
                .build();
    }

    public static LocationImportResult allErrors(int totalRows, List<LocationImportError> errors) {
        return LocationImportResult.builder()
                .totalRows(totalRows)
                .successCount(0)
                .errorCount(errors.size())
                .errors(errors)
                .createdLocations(List.of())
                .build();
    }
}
