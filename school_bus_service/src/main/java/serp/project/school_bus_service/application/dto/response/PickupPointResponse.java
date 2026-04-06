package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class PickupPointResponse extends BaseResponse {

    private Long schoolId;
    private String schoolName;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalTime pickupWindowStart;
    private LocalTime pickupWindowEnd;
}
