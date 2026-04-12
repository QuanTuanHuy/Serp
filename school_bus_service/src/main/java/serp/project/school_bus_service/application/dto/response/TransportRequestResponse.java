package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransportRequestResponse extends BaseResponse {

    private Long parentProfileId;
    private String parentProfileName;
    private Long schoolId;
    private String schoolName;
    private Double schoolLatitude;
    private Double schoolLongitude;
    private String requestType;
    private String status;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String notes;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
}
