/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

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
public class CreateModuleDto {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Code cannot be blank")
    private String code;
    private String description;
    @NotBlank(message = "Keycloak Client ID cannot be blank")
    private String keycloakClientId;
    private String category;
    private String icon;
    private Integer displayOrder;
    private String moduleType;
    private Boolean isGlobal;
    private Long organizationId;
    private Boolean isFree;
    private String pricingModel;
    private String status;
}
