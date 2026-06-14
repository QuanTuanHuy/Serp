package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class SchoolResponse extends BaseResponse {

    private String name;
    private String code;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private Double latitude;
    private Double longitude;

    private Integer pickupPointCount = 0;
    private Boolean hasCoordinates = false;
    private Boolean anyLinkedPointMissingCoordinates = false;
    private List<LinkedPickupPointSummaryResponse> pickupPoints = new ArrayList<>();
}
