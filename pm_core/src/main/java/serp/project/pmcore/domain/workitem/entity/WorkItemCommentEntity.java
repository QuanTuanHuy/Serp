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
public class WorkItemCommentEntity {
    private Long id;
    private Long tenantId;
    private Long workItemId;
    private Long authorId;
    private String body;
    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;
    private Long deletedAt;
}
