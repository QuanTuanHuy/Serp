package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.TransportPlanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransportPlanResponseDTO {
    private Long id;
    private Long truckId;
    private String truckCode;
    private Long driverId;
    private String driverName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TransportPlanStatus status;
    private int stopCount;
    private LocalDateTime createdStamp;
}
