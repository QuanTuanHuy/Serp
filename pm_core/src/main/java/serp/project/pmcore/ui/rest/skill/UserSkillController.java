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
import serp.project.pmcore.application.skill.UserSkillView;
import serp.project.pmcore.application.skill.command.user.replace.ReplaceUserSkillsCommand;
import serp.project.pmcore.application.skill.command.user.replace.ReplaceUserSkillsCommandHandler;
import serp.project.pmcore.application.skill.query.user.list.ListUserSkillsQuery;
import serp.project.pmcore.application.skill.query.user.list.ListUserSkillsQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.skill.dto.request.ReplaceUserSkillsRequest;

import java.util.List;

@RestController
@RequestMapping(PathConstants.USER_SKILLS)
@RequiredArgsConstructor
public class UserSkillController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final ListUserSkillsQueryHandler listUserSkillsQueryHandler;
    private final ReplaceUserSkillsCommandHandler replaceUserSkillsCommandHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<List<UserSkillView>>> listUserSkills(@PathVariable Long userId) {
        return ResponseEntity.ok(responseUtils.success(
                listUserSkillsQueryHandler.handle(new ListUserSkillsQuery(
                        userId,
                        requireTenantId()
                ))
        ));
    }

    @PutMapping
    public ResponseEntity<GeneralResponse<List<UserSkillView>>> replaceUserSkills(
            @PathVariable Long userId,
            @Valid @RequestBody ReplaceUserSkillsRequest request) {
        return ResponseEntity.ok(responseUtils.success(
                replaceUserSkillsCommandHandler.handle(new ReplaceUserSkillsCommand(
                        userId,
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
