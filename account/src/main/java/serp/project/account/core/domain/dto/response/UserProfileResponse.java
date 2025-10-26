/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;

import java.util.List;

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
    private UserType userType;
    private UserStatus status;
    private Long lastLoginAt;
    private String avatarUrl;
    private String timezone;
    private String preferredLanguage;
    private Long createdAt;
    private Long updatedAt;

    private List<String> roles;
}
