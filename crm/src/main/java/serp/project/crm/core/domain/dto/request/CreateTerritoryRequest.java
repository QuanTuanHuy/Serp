/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.TerritoryLevel;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateTerritoryRequest {
    @NotBlank(message = "Territory code is required")
    @Size(max = 50, message = "Territory code must not exceed 50 characters")
    private String territoryCode;

    @NotBlank(message = "Territory name is required")
    @Size(max = 255, message = "Territory name must not exceed 255 characters")
    private String territoryName;

    @NotNull(message = "Territory level is required")
    private TerritoryLevel territoryLevel;

    @Size(max = 10, message = "Country code must not exceed 10 characters")
    private String countryCode;

    @Size(max = 50, message = "Parent territory code must not exceed 50 characters")
    private String parentTerritoryCode;

    private Boolean active;
}
