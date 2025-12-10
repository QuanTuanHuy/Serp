/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {
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
    private String avatarUrl;
    private String timezone;
    private String preferredLanguage;

    private List<String> roles;

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public boolean belongsToOrganization(Long organizationId) {
        return this.organizationId != null && this.organizationId.equals(organizationId);
    }

    public boolean canBeAssignedToCrm() {
        boolean hasCrmRole = roles != null && roles.stream()
                .anyMatch(role -> role.contains("CRM"));
        return isActive() && hasCrmRole;
    }

    public String getRolesInCrm() {
        if (roles == null) {
            return "";
        }
        return String.join(", ", roles.stream()
                .filter(role -> role.contains("CRM"))
                .toList());
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
