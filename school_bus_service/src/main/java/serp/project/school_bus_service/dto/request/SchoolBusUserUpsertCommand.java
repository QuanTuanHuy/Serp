package serp.project.school_bus_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolBusUserUpsertCommand extends BaseCommandRequest {

    private Long tenantId;
    private Long accountUserId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;
    private Long primaryOrganizationId;
    private String preferredLanguage;
    private String timezone;
    private String userType;
    private String status;
    private String syncSource;
    private String rawPayloadJson;
    private java.util.List<String> roles;

}
