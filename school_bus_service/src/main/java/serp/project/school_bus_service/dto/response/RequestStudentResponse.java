package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStudentResponse extends BaseResponse {

    private Long requestId;
    private Long studentId;
    private String studentName;

    private Long pickupPointId;
    private String pickupPointName;
    private String pickupPointAddress;
    private Double pickupPointLatitude;
    private Double pickupPointLongitude;

    private Long dropoffPointId;
    private String dropoffPointName;
    private String dropoffPointAddress;
    private Double dropoffPointLatitude;
    private Double dropoffPointLongitude;

    private Long schoolScheduleId;
    private String schoolScheduleName;

    private String tripOption;

    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;

    private Long subscriptionId;
    private String subscriptionCode;
    private Long targetSubscriptionId;
    private String targetSubscriptionCode;

    private String studentNote;
}
