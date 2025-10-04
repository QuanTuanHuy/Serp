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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TeamEntity extends BaseEntity {
    private String name;
    private String description;
    private Long leaderId;
    private String notes;

    private List<TeamMemberEntity> members;

    // ========== Business Methods ==========

    public void updateFrom(TeamEntity updates) {
        if (updates.getName() != null) this.name = updates.getName();
        if (updates.getDescription() != null) this.description = updates.getDescription();
        if (updates.getLeaderId() != null) this.leaderId = updates.getLeaderId();
        if (updates.getNotes() != null) this.notes = updates.getNotes();
    }

    public void setDefaults() {
        // Simple entity, no complex defaults needed
    }
}
