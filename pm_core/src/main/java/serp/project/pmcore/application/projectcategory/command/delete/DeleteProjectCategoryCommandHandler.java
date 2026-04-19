/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.projectcategory.command.ProjectCategoryEventPayload;
import serp.project.pmcore.application.projectcategory.command.ProjectCategoryOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;

@Service
@RequiredArgsConstructor
public class DeleteProjectCategoryCommandHandler implements ICommandHandler<DeleteProjectCategoryCommand, DeleteProjectCategoryResult> {

    private final IProjectCategoryService projectCategoryService;
    private final ProjectCategoryOutboxPublisher outboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteProjectCategoryResult handle(DeleteProjectCategoryCommand command) {
        ProjectCategoryEntity deleted = projectCategoryService.deleteCategory(
                command.categoryId(),
                command.tenantId(),
                command.userId()
        );
        outboxPublisher.publishDeleted(command.tenantId(), ProjectCategoryEventPayload.from(deleted, command.userId()));
        return new DeleteProjectCategoryResult(
                deleted.getId(),
                true,
                deleted.getDeletedAt(),
                deleted.getUpdatedBy()
        );
    }
}
