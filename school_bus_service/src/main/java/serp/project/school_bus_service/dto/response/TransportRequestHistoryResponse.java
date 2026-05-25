package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransportRequestHistoryResponse extends BaseResponse {
    private Long requestId;
    private String oldStatus;
    private String newStatus;
    private Long changedBy;
    private LocalDateTime changedAt;
    private String reason;
    private String notes;
}

