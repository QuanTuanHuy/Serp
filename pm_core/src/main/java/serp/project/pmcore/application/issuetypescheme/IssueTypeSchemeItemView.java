/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;

public record IssueTypeSchemeItemView(
        Long id,
        Long issueTypeId,
        Integer sequence,
        IssueTypeSchemeIssueTypeView issueType
) {
    public static IssueTypeSchemeItemView from(IssueTypeSchemeItemEntity entity,
                                               IssueTypeSchemeIssueTypeView issueType) {
        return new IssueTypeSchemeItemView(
                entity.getId(),
                entity.getIssueTypeId(),
                entity.getSequence(),
                issueType
        );
    }
}
