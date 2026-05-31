package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DriverProfileResponse extends BaseResponse {

    private Long userId;
    private String fullName;
    private String phone;
    private String licenseNumber;
    private String licenseClass;
    private LocalDate licenseExpiryDate;
    private String status;
}
