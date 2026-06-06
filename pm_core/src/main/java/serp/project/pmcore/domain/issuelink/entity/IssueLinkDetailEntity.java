/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.issuelink.enums.IssueLinkDependencyBehavior;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IssueLinkDetailEntity {
    private Long linkId;
    private Long sourceId;
    private Long targetId;
    private Long linkTypeId;
    private String linkTypeName;
    private IssueLinkDependencyBehavior dependencyBehavior;
    private String outwardDescription;
    private String inwardDescription;
    private Long relatedWorkItemId;
    private Long relatedProjectId;
    private String relatedWorkItemKey;
    private String relatedWorkItemSummary;
    private Long relatedIssueTypeId;
    private String relatedIssueTypeName;
    private Long relatedStatusId;
    private String relatedStatusName;
    private Long createdAt;
    private Long createdBy;
}
