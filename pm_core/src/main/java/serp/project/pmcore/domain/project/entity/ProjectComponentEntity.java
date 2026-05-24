/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProjectComponentEntity extends BaseEntity {
    private Long tenantId;
    private Long projectId;
    private String name;
    private String description;
    private Long leadUserId;
    private String assigneeType;
    private Long issueCount;
    private Long deletedAt;
}
