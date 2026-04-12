package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

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
}
