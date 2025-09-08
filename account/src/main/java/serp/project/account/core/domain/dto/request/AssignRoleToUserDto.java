/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.domain.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AssignRoleToUserDto {
    @NotNull
    @Min(1)
    private Long userId;

    @NotEmpty
    private List<Long> roleIds;
}
