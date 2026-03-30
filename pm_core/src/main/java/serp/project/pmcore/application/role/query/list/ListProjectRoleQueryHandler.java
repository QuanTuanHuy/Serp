/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.query.list;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.port.IProjectRolePort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListProjectRoleQueryHandler implements IQueryHandler<ListProjectRoleQuery, List<ProjectRoleView>> {

    private final IProjectRolePort projectRolePort;

    @Override
    public List<ProjectRoleView> handle(ListProjectRoleQuery query) {
        return projectRolePort.getProjectRolesIncludingSystem(query.tenantId()).stream()
                .map(ProjectRoleView::from)
                .toList();
    }
}
