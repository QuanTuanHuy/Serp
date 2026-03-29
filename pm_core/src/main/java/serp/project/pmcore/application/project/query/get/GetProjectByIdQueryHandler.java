/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class GetProjectByIdQueryHandler implements IQueryHandler<GetProjectByIdQuery, ProjectDetailView> {
    private final IProjectReadPort projectReadPort;
    private final ProjectDetailViewFactory projectDetailViewFactory;

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailView handle(GetProjectByIdQuery query) {
        ProjectEntity project = projectReadPort.getProjectById(query.projectId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.project(query.projectId()));
        return projectDetailViewFactory.toView(project, query.tenantId(), query.expand());
    }
}
