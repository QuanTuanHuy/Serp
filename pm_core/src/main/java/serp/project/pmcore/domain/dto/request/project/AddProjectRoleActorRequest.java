/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProjectRoleActorRequest {
    @NotBlank(message = "Subject type is required")
    @Pattern(regexp = "^(USER|GROUP|SERVICE_ACCOUNT)$",
            message = "Subject type must be one of: USER, GROUP, SERVICE_ACCOUNT")
    private String subjectType;

    @NotBlank(message = "Subject id is required")
    private String subjectId;
}
