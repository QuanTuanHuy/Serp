/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateDepartmentRequest {
    private String name;

    private String description;

    private Long parentDepartmentId;

    private Long managerId;

    private List<Long> defaultModuleIds;

    private List<Long> defaultRoleIds;

    private Boolean isActive;
}
