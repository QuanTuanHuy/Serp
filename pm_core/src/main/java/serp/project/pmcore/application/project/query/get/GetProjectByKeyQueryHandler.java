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
public class GetProjectByKeyQueryHandler implements IQueryHandler<GetProjectByKeyQuery, ProjectDetailView> {
    private final IProjectReadPort projectReadPort;
    private final ProjectDetailViewAssembler projectDetailViewAssembler;

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailView handle(GetProjectByKeyQuery query) {
        ProjectEntity project = projectReadPort.getProjectByKey(query.key(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.projectByKey(query.key()));
        return projectDetailViewAssembler.toView(project, query.tenantId(), query.expand());
    }
}
