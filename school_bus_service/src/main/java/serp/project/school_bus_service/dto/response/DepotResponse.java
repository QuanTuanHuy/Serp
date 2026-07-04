package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepotResponse extends BaseResponse {

    private String code;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private String description;
}
