/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resourcecalendar.ResourceCalendarDeleteResult;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarExceptionPort;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class DeleteResourceCalendarExceptionCommandHandler
        implements ICommandHandler<DeleteResourceCalendarExceptionCommand, ResourceCalendarDeleteResult> {
    private final IResourceCalendarExceptionPort exceptionPort;
    private final ResourceCalendarExceptionMaterializationSupport materializationSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceCalendarDeleteResult handle(DeleteResourceCalendarExceptionCommand command) {
        ResourceCalendarExceptionEntity existing = exceptionPort.getExceptionById(command.tenantId(), command.exceptionId())
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                        "Resource calendar exception not found: id=" + command.exceptionId()));
        exceptionPort.deleteException(command.tenantId(), command.exceptionId());
        materializationSupport.rematerializeUser(command.tenantId(), existing.getUserId());
        return new ResourceCalendarDeleteResult(true);
    }
}
