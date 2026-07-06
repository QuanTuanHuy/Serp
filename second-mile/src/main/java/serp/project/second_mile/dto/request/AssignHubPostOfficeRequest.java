/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignHubPostOfficeRequest {

    @JsonProperty("post_office_codes")
    @Size(max = 500)
    private List<@NotBlank @Size(max = 255) String> postOfficeCodes;

    @AssertTrue(message = "post_office_codes is required")
    public boolean hasPostOfficeCodes() {
        return postOfficeCodes != null
                && postOfficeCodes.stream().anyMatch(code -> code != null && !code.trim().isEmpty());
    }
}
