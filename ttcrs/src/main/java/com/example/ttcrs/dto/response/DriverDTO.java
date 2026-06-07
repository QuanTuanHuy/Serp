package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.DriverStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverDTO {
    private Long userId;
    private String name;
    private String email;
    private DriverStatus status;
}
