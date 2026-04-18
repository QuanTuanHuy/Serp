/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.service.IStatusCategoryService;

@Service
@RequiredArgsConstructor
public class CreateStatusCategoryCommandHandler implements ICommandHandler<CreateStatusCategoryCommand, StatusCategoryView> {

    private final IStatusCategoryService statusCategoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatusCategoryView handle(CreateStatusCategoryCommand command) {
        StatusCategoryEntity created = statusCategoryService.createStatusCategory(
                StatusCategoryEntity.builder()
                        .name(command.name())
                        .key(command.key())
                        .color(command.color())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return StatusCategoryView.from(created, false);
    }
}
