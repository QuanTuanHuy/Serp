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

    // Driver execution fields
    private String cancelReason;
    private Integer currentStop;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Double totalDistance;
    private Integer totalTime;

    @Data
    @Builder
    public static class StopDTO {
        private Long id;
        private Integer sequence;
        private String locationCode;
        private Double lat;
        private Double lng;
        private StopAction action;
        private LocalDateTime plannedArrivalTime;
        private LocalDateTime actualArrivalTime;
        private Boolean isCompleted;
        private Long requestId;
        /** Non-null when requestId != null — used by frontend to decide evidenceAtSrc vs evidenceAtDest */
        private String requestSrcLocationCode;
        private String requestDestLocationCode;
    }
}
