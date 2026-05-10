/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPostOfficeHubRequest {

    /**
     * Second-mile hub id; null clears assignment.
     */
    @JsonProperty("hub_id")
    private Long hubId;
}
