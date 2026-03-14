/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.UserType;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserInvitationEntity extends BaseEntity {
    private Long organizationId;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private List<Long> roleIds;
    private Long departmentId;
    private List<Long> moduleIds;
    private String message;
    private String token;
    private String status;
    private Long invitedBy;
    private Long invitedAt;
    private Long expiresAt;
    private Long acceptedAt;

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    public boolean isExpired() {
        return ("PENDING".equalsIgnoreCase(this.status) &&
                this.expiresAt != null &&
                System.currentTimeMillis() > this.expiresAt) ||
                "EXPIRED".equalsIgnoreCase(this.status);
    }
}
