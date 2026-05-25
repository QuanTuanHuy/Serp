/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.entity;

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
public class WorkItemHistoryEntity extends BaseEntity {
    private Long tenantId;
    private Long workItemId;
    private Long actorId;
    private String fieldKey;
    private String fieldName;
    private String fromValue;
    private String toValue;
    private String fromDisplayValue;
    private String toDisplayValue;
    private Long deletedAt;
}
