/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.delete;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;

public record DeleteIssueTypeSchemeResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteIssueTypeSchemeResult from(IssueTypeSchemeEntity entity) {
        return new DeleteIssueTypeSchemeResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
