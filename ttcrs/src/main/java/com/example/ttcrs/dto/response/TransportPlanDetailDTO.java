package com.example.ttcrs.dto.response;

import com.example.ttcrs.constant.StopAction;
import com.example.ttcrs.constant.TransportPlanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransportPlanDetailDTO {

    private Long id;
    private Long truckId;
    private String truckCode;
    private Long driverId;
    private String driverName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TransportPlanStatus status;
    private LocalDateTime createdStamp;
    private List<StopDTO> stops;

    @Data
    @Builder
    public static class StopDTO {
        private Long id;
        private Integer sequence;
        private String locationCode;
        private StopAction action;
        private LocalDateTime plannedArrivalTime;
        private LocalDateTime actualArrivalTime;
        private Long requestId;
    }
}
