/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostOfficeHandoverManifestRequest {
    @JsonProperty("post_office_id")
    @NotNull
    private Long postOfficeId;

    @JsonProperty("target_hub_id")
    private Long targetHubId;

    @JsonProperty("order_codes")
    @NotEmpty
    private List<String> orderCodes;

    private String note;
}
