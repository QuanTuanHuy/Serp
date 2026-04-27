/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.command.delete;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;

public record DeleteIssueTypeResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
    public static DeleteIssueTypeResult from(IssueTypeEntity entity) {
        return new DeleteIssueTypeResult(
                entity.getId(),
                true,
                entity.getDeletedAt(),
                entity.getUpdatedBy()
        );
    }
}
