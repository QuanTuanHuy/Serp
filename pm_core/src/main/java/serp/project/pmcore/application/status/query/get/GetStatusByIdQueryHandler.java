/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

@Service
@RequiredArgsConstructor
public class GetStatusByIdQueryHandler implements IQueryHandler<GetStatusByIdQuery, StatusView> {

    private final IStatusService statusService;

    @Override
    @Transactional(readOnly = true)
    public StatusView handle(GetStatusByIdQuery query) {
        StatusEntity status = statusService.getVisibleStatusById(query.statusId(), query.tenantId());
        return StatusView.from(status, Boolean.TRUE.equals(status.getIsSystem()));
    }
}
