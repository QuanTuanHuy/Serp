/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEventPayload {

    @JsonProperty("projectId")
    private Long projectId;

    @JsonProperty("projectKey")
    private String projectKey;

    @JsonProperty("projectName")
    private String projectName;

    @JsonProperty("projectTypeKey")
    private String projectTypeKey;

    @JsonProperty("isArchived")
    private Boolean isArchived;
}
