package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StudentSubscriptionHistoryResponse extends BaseResponse {

    private Long subscriptionId;
    private Long sourceRequestId;
    private Long requestStudentId;

    private String changeType;

    private String oldStatus;
    private String newStatus;

    private Long oldPickupPointId;
    private Long newPickupPointId;

    private Long oldDropoffPointId;
    private Long newDropoffPointId;

    private Long oldSchoolScheduleId;
    private Long newSchoolScheduleId;

    private String oldTripOption;
    private String newTripOption;

    private LocalDate oldEffectiveFrom;
    private LocalDate newEffectiveFrom;
    private LocalDate oldEffectiveTo;
    private LocalDate newEffectiveTo;

    private Long changedBy;
    private LocalDateTime changedAt;

    private String reason;
    private String notes;
}
