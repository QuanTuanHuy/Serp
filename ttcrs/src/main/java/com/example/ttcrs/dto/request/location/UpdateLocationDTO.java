package com.example.ttcrs.dto.request.location;

import com.example.ttcrs.constant.LocationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating an existing Location.
 * All fields are optional — only non-null values are applied.
 */
@Getter
@Setter
public class UpdateLocationDTO {

    private LocationType type;

    @DecimalMin(value = "-90.0",  message = "lat phải >= -90")
    @DecimalMax(value = "90.0",   message = "lat phải <= 90")
    private Double lat;

    @DecimalMin(value = "-180.0", message = "lng phải >= -180")
    @DecimalMax(value = "180.0",  message = "lng phải <= 180")
    private Double lng;
}
