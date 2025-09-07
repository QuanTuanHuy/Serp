package serp.project.account.core.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AddPermissionToRoleDto {
    @NotEmpty
    private List<Long> permissionIds;
}
