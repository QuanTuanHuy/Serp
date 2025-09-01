package serp.project.account.core.domain.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    @NotEmpty
    private List<Long> permissionIds;
}
