/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignUserToDepartmentRequest {
    @NotNull(message = "User ID must not be null")
    private Long userId;

    private String jobTitle;

    @Builder.Default
    private Boolean isPrimary = false;

    @NotNull(message = "Department ID must not be null")
    private Long departmentId;
}
