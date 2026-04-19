/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.projectcategory.ProjectCategoryView;
import serp.project.pmcore.application.projectcategory.command.ProjectCategoryEventPayload;
import serp.project.pmcore.application.projectcategory.command.ProjectCategoryOutboxPublisher;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;

@Service
@RequiredArgsConstructor
public class UpdateProjectCategoryCommandHandler implements ICommandHandler<UpdateProjectCategoryCommand, ProjectCategoryView> {

    private final IProjectCategoryService projectCategoryService;
    private final ProjectCategoryOutboxPublisher outboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectCategoryView handle(UpdateProjectCategoryCommand command) {
        ProjectCategoryEntity updated = projectCategoryService.updateCategory(
                command.categoryId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        outboxPublisher.publishUpdated(command.tenantId(), ProjectCategoryEventPayload.from(updated, command.userId()));
        return ProjectCategoryView.from(updated);
    }
}
