/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.workitem.service.IResolutionService;

@Service
@RequiredArgsConstructor
public class GetResolutionByIdQueryHandler implements IQueryHandler<GetResolutionByIdQuery, ResolutionView> {

    private final IResolutionService resolutionService;

    @Override
    @Transactional(readOnly = true)
    public ResolutionView handle(GetResolutionByIdQuery query) {
        return ResolutionView.from(resolutionService.getVisibleResolutionById(query.id(), query.tenantId()));
    }
}
