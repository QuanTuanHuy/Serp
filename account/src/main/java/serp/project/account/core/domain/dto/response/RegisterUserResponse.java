/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RegisterUserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Long primaryOrganizationId;
    private UserType userType;
    private UserStatus status;
}
