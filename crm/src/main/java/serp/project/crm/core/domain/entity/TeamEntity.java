/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.TeamStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TeamEntity extends BaseEntity {
    private String name;
    private String description;
    private Long managerUserId;
    private String notes;
    private TeamStatus status;
    private Long lastAssignedMemberUserId;

    private List<TeamMemberEntity> members;

    public void updateFrom(TeamEntity updates) {
        if (updates.getName() != null)
            this.name = updates.getName();
        if (updates.getDescription() != null)
            this.description = updates.getDescription();
        if (updates.getManagerUserId() != null)
            this.managerUserId = updates.getManagerUserId();
        if (updates.getNotes() != null)
            this.notes = updates.getNotes();
        if (updates.getStatus() != null)
            this.status = updates.getStatus();
        if (updates.getLastAssignedMemberUserId() != null)
            this.lastAssignedMemberUserId = updates.getLastAssignedMemberUserId();
    }

    public void setDefaults() {
        if (this.status == null) {
            this.status = TeamStatus.ACTIVE;
        }
    }
}
