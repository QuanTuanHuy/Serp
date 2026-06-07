/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductTypeRequest {

    @JsonProperty("code")
    @NotBlank
    private String code;

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("is_active")
    private Boolean isActive;
}
