package serp.project.account.core.domain.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRoleDto {
    @NotBlank
    private String name;
    private String description;
    @Builder.Default
    private Boolean isRealmRole = false;
    private String keycloakClientId;
    private Integer priority;

    private List<Long> permissionIds;
}
