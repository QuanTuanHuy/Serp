/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.skill;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.skill.SkillView;
import serp.project.pmcore.application.skill.command.archive.ArchiveSkillCommand;
import serp.project.pmcore.application.skill.command.archive.ArchiveSkillCommandHandler;
import serp.project.pmcore.application.skill.command.create.CreateSkillCommand;
import serp.project.pmcore.application.skill.command.create.CreateSkillCommandHandler;
import serp.project.pmcore.application.skill.command.update.UpdateSkillCommand;
import serp.project.pmcore.application.skill.command.update.UpdateSkillCommandHandler;
import serp.project.pmcore.application.skill.query.list.ListSkillsQuery;
import serp.project.pmcore.application.skill.query.list.ListSkillsQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.skill.dto.request.CreateSkillRequest;
import serp.project.pmcore.ui.rest.skill.dto.request.UpdateSkillRequest;

import java.util.List;

@RestController
@RequestMapping(PathConstants.SKILLS)
@RequiredArgsConstructor
public class SkillController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final ListSkillsQueryHandler listSkillsQueryHandler;
    private final CreateSkillCommandHandler createSkillCommandHandler;
    private final UpdateSkillCommandHandler updateSkillCommandHandler;
    private final ArchiveSkillCommandHandler archiveSkillCommandHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<List<SkillView>>> listSkills() {
        return ResponseEntity.ok(responseUtils.success(
                listSkillsQueryHandler.handle(new ListSkillsQuery(requireTenantId()))
        ));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<SkillView>> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        SkillView response = createSkillCommandHandler.handle(new CreateSkillCommand(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                requireTenantId(),
                requireUserId()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PatchMapping("/{skillId}")
    public ResponseEntity<GeneralResponse<SkillView>> updateSkill(
            @PathVariable Long skillId,
            @Valid @RequestBody UpdateSkillRequest request) {
        SkillView response = updateSkillCommandHandler.handle(new UpdateSkillCommand(
                skillId,
                request.getCode(),
                request.getName(),
                request.getDescription(),
                requireTenantId(),
                requireUserId()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<GeneralResponse<SkillView>> archiveSkill(@PathVariable Long skillId) {
        return ResponseEntity.ok(responseUtils.success(
                archiveSkillCommandHandler.handle(new ArchiveSkillCommand(
                        skillId,
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
