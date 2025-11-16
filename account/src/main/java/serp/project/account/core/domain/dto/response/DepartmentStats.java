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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentStats {
    private Integer totalDepartments;
    private Integer totalMembers;
    private Double averageTeamSize;
    private Integer departmentsWithManagers;
    private Integer activeDepartments;
    private Integer inactiveDepartments;
}
