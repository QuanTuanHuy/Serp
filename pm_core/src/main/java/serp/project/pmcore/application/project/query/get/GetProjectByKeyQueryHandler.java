/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IProjectPort;

@Service
@RequiredArgsConstructor
public class GetProjectByKeyQueryHandler implements IQueryHandler<GetProjectByKeyQuery, ProjectDetailView> {
    private final IProjectPort projectPort;
    private final ProjectDetailViewFactory projectDetailViewFactory;

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailView handle(GetProjectByKeyQuery query) {
        ProjectEntity project = projectPort.getProjectByKey(query.key(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.projectByKey(query.key()));
        return projectDetailViewFactory.toView(project, query.tenantId(), query.expand());
    }
}
