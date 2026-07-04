package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class SchoolBusUserResponse extends BaseResponse {

    private Long accountUserId;
    private String keycloakId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String status;
    private String userType;
    private LocalDateTime lastSyncedAt;
    private String syncSource;

}
