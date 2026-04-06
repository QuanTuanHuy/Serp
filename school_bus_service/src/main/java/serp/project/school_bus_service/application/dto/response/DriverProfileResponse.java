package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverProfileResponse extends BaseResponse {

    private Long userId;
    private String fullName;
    private String phone;
    private String licenseNumber;
    private String status;
}
