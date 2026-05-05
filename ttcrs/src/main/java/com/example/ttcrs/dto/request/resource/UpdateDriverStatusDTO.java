package com.example.ttcrs.dto.request.resource;

import com.example.ttcrs.constant.DriverStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDriverStatusDTO {

    @NotNull(message = "status không được để trống")
    private DriverStatus status;
}
