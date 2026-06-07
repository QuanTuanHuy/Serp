package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionPausePeriodResponse extends BaseResponse {

    private Long subscriptionId;
    private Long sourceRequestId;
    private Long requestStudentId;

    private LocalDate pauseFrom;
    private LocalDate pauseTo;

    private String status;

    private String reason;
}
