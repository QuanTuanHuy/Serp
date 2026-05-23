/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WorkItemHistoryEntity {
    private Long id;
    private Long tenantId;
    private Long workItemId;
    private Long actorId;
    private String fieldKey;
    private String fieldName;
    private String fromValue;
    private String toValue;
    private String fromDisplayValue;
    private String toDisplayValue;
    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;
    private Long deletedAt;
}
