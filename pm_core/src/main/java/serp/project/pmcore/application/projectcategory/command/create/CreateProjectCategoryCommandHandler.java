/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command.create;

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
public class CreateProjectCategoryCommandHandler implements ICommandHandler<CreateProjectCategoryCommand, ProjectCategoryView> {

    private final IProjectCategoryService projectCategoryService;
    private final ProjectCategoryOutboxPublisher outboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectCategoryView handle(CreateProjectCategoryCommand command) {
        ProjectCategoryEntity created = projectCategoryService.createCategory(
                ProjectCategoryEntity.builder()
                        .name(command.name())
                        .description(command.description())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        outboxPublisher.publishCreated(command.tenantId(), ProjectCategoryEventPayload.from(created, command.userId()));
        return ProjectCategoryView.from(created);
    }
}
