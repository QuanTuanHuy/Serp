package com.example.ttcrs.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationImportError {

    /** 1-based row number in Excel (excluding header). */
    private int row;

    /** Field name with the error, e.g. "Location Code", "Type", "Latitude", "Longitude". */
    private String field;

    /** Human-readable error message. */
    private String message;
}
