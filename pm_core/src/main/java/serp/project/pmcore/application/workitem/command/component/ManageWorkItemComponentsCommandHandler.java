/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.WorkItemComponentView;
import serp.project.pmcore.application.workitem.support.WorkItemComponentAccessHelper;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageWorkItemComponentsCommandHandler
        implements ICommandHandler<ManageWorkItemComponentsCommand, List<WorkItemComponentView>> {

    private final WorkItemComponentAccessHelper accessHelper;
    private final IProjectComponentService projectComponentService;
    private final IWorkItemWritePort workItemWritePort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkItemComponentView> handle(ManageWorkItemComponentsCommand command) {
        accessHelper.requireEditableWorkItem(
                command.projectId(),
                command.workItemId(),
                command.tenantId(),
                command.userId(),
                command.groupKeys()
        );

        List<ProjectComponentEntity> requestedComponents = command.componentIds().stream()
                .map(componentId -> projectComponentService.getComponentById(
                        componentId,
                        command.projectId(),
                        command.tenantId()
                ))
                .toList();
        accessHelper.validateComponentsBelongToProject(requestedComponents, command.projectId());

        workItemWritePort.addWorkItemComponents(
                command.workItemId(),
                command.tenantId(),
                command.userId(),
                command.componentIds()
        );

        return workItemReadPort.getActiveComponentsByWorkItemId(command.workItemId(), command.tenantId())
                .stream()
                .map(WorkItemComponentView::from)
                .toList();
    }
}
