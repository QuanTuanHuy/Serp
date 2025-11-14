/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDepartmentRequest {
    @NotBlank
    private String name;

    private String description;

    private Long parentDepartmentId;

    private Long managerId;

    @Builder.Default
    private List<Long> defaultModuleIds = Collections.emptyList();

    @Builder.Default
    private List<Long> defaultRoleIds = Collections.emptyList(); // not use for now
}
