/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.TeamMemberStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TeamMemberEntity extends BaseEntity {
    private String name;
    private String email;
    private String phone;
    private Long teamId;
    private Long userId;
    private String role;
    private TeamMemberStatus status;

    public void updateFrom(TeamMemberEntity updates) {
        if (updates.getName() != null)
            this.name = updates.getName();
        if (updates.getEmail() != null)
            this.email = updates.getEmail();
        if (updates.getPhone() != null)
            this.phone = updates.getPhone();
        if (updates.getRole() != null)
            this.role = updates.getRole();
    }

    public void setDefaults() {
        if (this.status == null) {
            this.status = TeamMemberStatus.INVITED;
        }
    }

    public void confirmMember(Long tenantId) {
        if (TeamMemberStatus.CONFIRMED.equals(this.status)) {
            throw new IllegalStateException("Team member is already confirmed");
        }
        this.status = TeamMemberStatus.CONFIRMED;
        this.setTenantId(tenantId);
    }

    public void archiveMember(Long tenantId) {
        if (TeamMemberStatus.ARCHIVED.equals(this.status)) {
            throw new IllegalStateException("Team member is already archived");
        }
        this.status = TeamMemberStatus.ARCHIVED;
        this.setTenantId(tenantId);
    }

    public void changeRole(String newRole, Long tenantId) {
        if (newRole == null || newRole.isBlank()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        if (newRole.equals(this.role)) {
            throw new IllegalStateException("Member already has role: " + newRole);
        }
        this.role = newRole;
        this.setTenantId(tenantId);
    }
}
