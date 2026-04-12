/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.roleactor.add;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.application.project.command.roleactor.RoleActorSubjectValidator;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;

@Component
@RequiredArgsConstructor
public class AddProjectRoleActorValidator {

    private final RoleActorSubjectValidator roleActorSubjectValidator;

    public ValidatedRoleActorSubject validate(AddProjectRoleActorCommand command) {
        ProjectRoleActorSubjectType subjectType = roleActorSubjectValidator
                .validateAndNormalizeSubjectType(command.subjectType());
        String normalizedSubjectId = roleActorSubjectValidator
                .validateAndNormalizeSubjectId(command.subjectId());

        roleActorSubjectValidator.validateSubjectExistsForAdd(subjectType, normalizedSubjectId);

        return new ValidatedRoleActorSubject(subjectType.name(), normalizedSubjectId);
    }

    public record ValidatedRoleActorSubject(String subjectType, String subjectId) {
    }
}
