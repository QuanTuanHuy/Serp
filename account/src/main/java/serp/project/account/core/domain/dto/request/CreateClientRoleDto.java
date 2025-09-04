package serp.project.account.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateClientRoleDto {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String clientId;
}
