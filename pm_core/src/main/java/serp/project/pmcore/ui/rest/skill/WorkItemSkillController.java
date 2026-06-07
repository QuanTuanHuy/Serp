/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.skill;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.skill.WorkItemSkillView;
import serp.project.pmcore.application.skill.command.workitem.replace.ReplaceWorkItemSkillsCommand;
import serp.project.pmcore.application.skill.command.workitem.replace.ReplaceWorkItemSkillsCommandHandler;
import serp.project.pmcore.application.skill.query.workitem.list.ListWorkItemSkillsQuery;
import serp.project.pmcore.application.skill.query.workitem.list.ListWorkItemSkillsQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.skill.dto.request.ReplaceWorkItemSkillsRequest;

import java.util.List;

@RestController
@RequestMapping(PathConstants.WORKITEM_SKILLS)
@RequiredArgsConstructor
public class WorkItemSkillController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final ListWorkItemSkillsQueryHandler listWorkItemSkillsQueryHandler;
    private final ReplaceWorkItemSkillsCommandHandler replaceWorkItemSkillsCommandHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<List<WorkItemSkillView>>> listWorkItemSkills(
            @PathVariable Long projectId,
            @PathVariable Long workItemId) {
        return ResponseEntity.ok(responseUtils.success(
                listWorkItemSkillsQueryHandler.handle(new ListWorkItemSkillsQuery(
                        projectId,
                        workItemId,
                        requireTenantId()
                ))
        ));
    }

    @PutMapping
    public ResponseEntity<GeneralResponse<List<WorkItemSkillView>>> replaceWorkItemSkills(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @Valid @RequestBody ReplaceWorkItemSkillsRequest request) {
        return ResponseEntity.ok(responseUtils.success(
                replaceWorkItemSkillsCommandHandler.handle(new ReplaceWorkItemSkillsCommand(
                        projectId,
                        workItemId,
                        request.toData(),
                        requireTenantId(),
                        requireUserId()
                ))
        ));
    }

    private Long requireUserId() {
        return authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
    }

    private Long requireTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
