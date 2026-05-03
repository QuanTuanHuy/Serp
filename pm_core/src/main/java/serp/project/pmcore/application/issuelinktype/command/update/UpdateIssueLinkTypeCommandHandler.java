/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;

@Service
@RequiredArgsConstructor
public class UpdateIssueLinkTypeCommandHandler implements ICommandHandler<UpdateIssueLinkTypeCommand, IssueLinkTypeView> {

    private final IIssueLinkTypeService issueLinkTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueLinkTypeView handle(UpdateIssueLinkTypeCommand command) {
        IssueLinkTypeEntity updated = issueLinkTypeService.update(
                command.id(),
                IssueLinkTypeEntity.builder()
                        .name(command.name())
                        .outwardDescription(command.outwardDescription())
                        .inwardDescription(command.inwardDescription())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return IssueLinkTypeView.from(updated);
    }
}
