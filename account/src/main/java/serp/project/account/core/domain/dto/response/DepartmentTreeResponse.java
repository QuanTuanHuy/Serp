/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentTreeResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long managerId;
    private String managerName;
    private Integer memberCount;
    private Boolean isActive;
    private List<DepartmentTreeResponse> children;
}
