/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

@Service
@RequiredArgsConstructor
public class CreateStatusCommandHandler implements ICommandHandler<CreateStatusCommand, StatusView> {

    private final IStatusService statusService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatusView handle(CreateStatusCommand command) {
        StatusEntity created = statusService.createStatus(
                StatusEntity.builder()
                        .statusKey(command.statusKey())
                        .name(command.name())
                        .description(command.description())
                        .iconUrl(command.iconUrl())
                        .categoryId(command.statusCategoryId())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return StatusView.from(created, false);
    }
}
