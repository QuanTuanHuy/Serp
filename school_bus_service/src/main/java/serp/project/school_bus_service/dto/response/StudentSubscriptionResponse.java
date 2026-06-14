package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentSubscriptionResponse extends BaseResponse {
    private String subscriptionCode;
    private Long studentId;
    private String studentName;
    private Long schoolId;
    private String schoolName;
    private Long pickupPointId;
    private String pickupPointName;
    private Long dropoffPointId;
    private String dropoffPointName;
    private String tripOption;
    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private Long sourceRequestId;
    private String sourceRequestCode;
    private String studentCode;
    private String parentName;
    private String schoolCode;
    private String pickupPointCode;
    private String dropoffPointCode;
}

