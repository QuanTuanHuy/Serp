package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolPickupPointResponse extends BaseResponse {

    private Long schoolId;
    private String schoolName;
    private Long pickupPointId;
    private String pickupPointName;
    private String pickupPointAddress;
    private Double pickupPointLatitude;
    private Double pickupPointLongitude;
    private String pickupPointUsageType;
    private Boolean isDefault;
}
