/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;

@Service
@RequiredArgsConstructor
public class CreateIssueLinkTypeCommandHandler implements ICommandHandler<CreateIssueLinkTypeCommand, IssueLinkTypeView> {

    private final IIssueLinkTypeService issueLinkTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueLinkTypeView handle(CreateIssueLinkTypeCommand command) {
        IssueLinkTypeEntity created = issueLinkTypeService.create(
                IssueLinkTypeEntity.builder()
                        .name(command.name())
                        .outwardDescription(command.outwardDescription())
                        .inwardDescription(command.inwardDescription())
                        .build(),
                command.tenantId(),
                command.userId()
        );
        return IssueLinkTypeView.from(created);
    }
}
