package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickupPointResponse extends BaseResponse {

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String code;
    private String zoneCode;
    private String usageType;
    private String pickupInstruction;

    // Legacy fields - always null, kept for backward compatibility
    @Deprecated
    private Long schoolId;
    @Deprecated
    private String schoolName;
}
