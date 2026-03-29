/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IssueTypeEntity extends BaseEntity {
    private Long tenantId;
    private String typeKey;
    private String name;
    private String description;
    private String iconUrl;
    private Integer hierarchyLevel;
    private boolean isSystem;
}
