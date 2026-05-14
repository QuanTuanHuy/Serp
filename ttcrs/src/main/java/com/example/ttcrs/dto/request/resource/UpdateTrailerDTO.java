package com.example.ttcrs.dto.request.resource;

import com.example.ttcrs.constant.VehicleStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating an existing Trailer.
 * All fields are optional — only non-null values are applied.
 */
@Getter
@Setter
public class UpdateTrailerDTO {

    private VehicleStatus status;
    private String currentLocationCode;
}
