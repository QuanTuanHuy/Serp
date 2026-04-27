/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.service.IStatusCategoryService;

@Service
@RequiredArgsConstructor
public class DeleteStatusCategoryCommandHandler implements ICommandHandler<DeleteStatusCategoryCommand, DeleteStatusCategoryResult> {

    private final IStatusCategoryService statusCategoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteStatusCategoryResult handle(DeleteStatusCategoryCommand command) {
        StatusCategoryEntity deleted = statusCategoryService.deleteStatusCategory(
                command.statusCategoryId(),
                command.tenantId(),
                command.userId()
        );
        return DeleteStatusCategoryResult.from(deleted);
    }
}
