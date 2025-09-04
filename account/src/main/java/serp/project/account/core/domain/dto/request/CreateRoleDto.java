package serp.project.account.core.domain.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateRoleDto {
    @NotBlank
    private String name;
    private String description;

    private List<Long> permissionIds;
}
