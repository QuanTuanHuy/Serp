/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalRoutePlanRequest {
    @NotBlank
    @JsonProperty("origin_post_office_code")
    private String originPostOfficeCode;

    @NotBlank
    @JsonProperty("destination_post_office_code")
    private String destinationPostOfficeCode;
}
