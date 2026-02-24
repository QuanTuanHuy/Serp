/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemEntity extends BaseEntity {
    private Long tenantId;
    private Long projectId;
    private Long issueTypeId;

    private Long issueNo;
    private String key;
    private String summary;
    private String description;

    private Long statusId;
    private Long priorityId;
    private Long assigneeId;
    private Long reporterId;
    private Long parentId;

    private Long dueDate;
    private String rank;

    private Long timeOriginalEstimate;
    private Long timeRemainingEstimate;
    private Long timeSpent;

    // ---- Enrichment display fields
    private String issueTypeName;
    private String issueTypeIconUrl;
    private Integer issueTypeHierarchyLevel;

    private String priorityName;
    private String priorityIconUrl;
    private String priorityColor;
    private Integer prioritySequence;
}
