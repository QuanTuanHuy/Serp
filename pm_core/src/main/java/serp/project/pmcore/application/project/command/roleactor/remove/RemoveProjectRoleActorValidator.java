/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.remove;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.application.project.command.roleactor.RoleActorSubjectValidator;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;

@Component
@RequiredArgsConstructor
public class RemoveProjectRoleActorValidator {

    private final RoleActorSubjectValidator roleActorSubjectValidator;

    public ValidatedRoleActorSubject validate(RemoveProjectRoleActorCommand command) {
        ProjectRoleActorSubjectType subjectType = roleActorSubjectValidator
                .validateAndNormalizeSubjectType(command.subjectType());
        String normalizedSubjectId = roleActorSubjectValidator
                .validateAndNormalizeSubjectId(command.subjectId());

        return new ValidatedRoleActorSubject(subjectType.name(), normalizedSubjectId);
    }

    public record ValidatedRoleActorSubject(String subjectType, String subjectId) {
    }
}
