/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.statuscategory.StatusCategoryView;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.service.IStatusCategoryService;

@Service
@RequiredArgsConstructor
public class UpdateStatusCategoryCommandHandler implements ICommandHandler<UpdateStatusCategoryCommand, StatusCategoryView> {

    private final IStatusCategoryService statusCategoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatusCategoryView handle(UpdateStatusCategoryCommand command) {
        StatusCategoryEntity updated = statusCategoryService.updateStatusCategory(
                command.statusCategoryId(),
                command.data(),
                command.tenantId(),
                command.userId()
        );
        return StatusCategoryView.from(updated, false);
    }
}
