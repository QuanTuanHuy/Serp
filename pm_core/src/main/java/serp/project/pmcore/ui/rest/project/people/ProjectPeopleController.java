/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.people;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.project.command.people.remove.RemoveProjectPersonCommand;
import serp.project.pmcore.application.project.command.people.remove.RemoveProjectPersonCommandHandler;
import serp.project.pmcore.application.project.command.people.replace.ReplaceProjectPersonRolesCommand;
import serp.project.pmcore.application.project.command.people.replace.ReplaceProjectPersonRolesCommandHandler;
import serp.project.pmcore.application.project.query.people.list.ListProjectPeopleQuery;
import serp.project.pmcore.application.project.query.people.list.ListProjectPeopleQueryHandler;
import serp.project.pmcore.application.project.query.people.list.ProjectPeopleView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.project.people.dto.request.ReplaceProjectPersonRolesRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(PathConstants.PROJECT_PEOPLE)
@RequiredArgsConstructor
public class ProjectPeopleController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final ListProjectPeopleQueryHandler listProjectPeopleQueryHandler;
    private final ReplaceProjectPersonRolesCommandHandler replaceProjectPersonRolesCommandHandler;
    private final RemoveProjectPersonCommandHandler removeProjectPersonCommandHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<List<ProjectPeopleView>>> listProjectPeople(@PathVariable Long projectId) {
        List<ProjectPeopleView> people = listProjectPeopleQueryHandler.handle(new ListProjectPeopleQuery(
                projectId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(people));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<GeneralResponse<?>> replaceProjectPersonRoles(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody ReplaceProjectPersonRolesRequest request) {
        replaceProjectPersonRolesCommandHandler.handle(new ReplaceProjectPersonRolesCommand(
                projectId,
                userId,
                request.getRoleIds(),
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.status("Project person roles updated"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<GeneralResponse<?>> removeProjectPerson(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        removeProjectPersonCommandHandler.handle(new RemoveProjectPersonCommand(
                projectId,
                userId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.status("Project person removed"));
    }

    private Long requireCurrentUserId() {
        return authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
