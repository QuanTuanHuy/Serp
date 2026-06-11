package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendantProfileResponse extends BaseResponse {

    private Long userId;
    private Long schoolBusUserId;
    private Long accountUserId;
    private String fullName;
    private String phone;
    private String status;
    private SchoolBusUserResponse user;
}
