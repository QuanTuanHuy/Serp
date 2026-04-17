package com.example.ttcrs.dto.request.resource;

import com.example.ttcrs.constant.DriverStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating an existing Driver.
 * All fields are optional — only non-null values are applied.
 */
@Getter
@Setter
public class UpdateDriverDTO {

    private String name;
    private DriverStatus status;
}
