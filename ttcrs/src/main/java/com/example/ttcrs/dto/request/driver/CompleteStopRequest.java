package com.example.ttcrs.dto.request.driver;

import lombok.Data;

@Data
public class CompleteStopRequest {
    /** MinIO URL of evidence photo; nullable when stop has no associated request */
    private String evidenceUrl;
    /** Total route distance (km) — only provided when completing the last stop */
    private Double totalDistance;
}
