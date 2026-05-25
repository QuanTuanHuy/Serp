/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.dto.user;

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
public class UserProfileDto {
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
    private Long primaryDepartmentId;
    private String primaryDepartmentName;
    private Integer moduleAccessCount;

    public boolean isActive() {
        return status != null && "ACTIVE".equalsIgnoreCase(status);
    }

    public String getFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }
}
