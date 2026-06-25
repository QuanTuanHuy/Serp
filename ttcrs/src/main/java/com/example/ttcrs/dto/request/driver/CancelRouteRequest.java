package com.example.ttcrs.dto.request.driver;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelRouteRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
