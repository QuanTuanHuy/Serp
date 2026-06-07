package serp.project.school_bus_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * DTO representing user information fetched from Core Account API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountUserResponse {

    private Long id;
    
    private String keycloakId;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String phoneNumber;
    
    private Long organizationId;
    
    private String organizationName;
    
    private String userType;
    
    private String status;
    
    private Long lastLoginAt;
    
    private String avatarUrl;
    
    private String timezone;
    
    private String preferredLanguage;
    
    private Long createdAt;
    
    private Long updatedAt;

    private List<String> roles;

}
