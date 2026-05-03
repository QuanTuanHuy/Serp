/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;

@Service
@RequiredArgsConstructor
public class DeleteIssueLinkTypeCommandHandler implements ICommandHandler<DeleteIssueLinkTypeCommand, DeleteIssueLinkTypeResult> {

    private final IIssueLinkTypeService issueLinkTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeleteIssueLinkTypeResult handle(DeleteIssueLinkTypeCommand command) {
        IssueLinkTypeEntity deleted = issueLinkTypeService.delete(command.id(), command.tenantId(), command.userId());
        return DeleteIssueLinkTypeResult.from(deleted);
    }
}
