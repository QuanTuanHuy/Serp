package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedPickupPointSummaryResponse {
    private Long id;
    private Long linkId;
    private String code;
    private String name;
    private String address;
    private String usageType;
    private Double latitude;
    private Double longitude;
    private Boolean hasCoordinates;
    private Boolean isDefault;
}
