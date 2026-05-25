/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeView;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;

@Service
@RequiredArgsConstructor
public class ListWorkflowSchemesQueryHandler implements IQueryHandler<ListWorkflowSchemesQuery, PageView<WorkflowSchemeView>> {

    private final IWorkflowSchemeService workflowSchemeService;

    @Override
    @Transactional(readOnly = true)
    public PageView<WorkflowSchemeView> handle(ListWorkflowSchemesQuery query) {
        WorkflowSchemeListCriteria criteria = query.toCriteria();
        return PageViews.from(
                workflowSchemeService.listVisibleWorkflowSchemes(query.tenantId(), criteria),
                criteria,
                scheme -> WorkflowSchemeView.from(scheme, scheme.isSystem())
        );
    }
}
