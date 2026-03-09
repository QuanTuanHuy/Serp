/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {

    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;

    @Size(max = 10000, message = "Description must be at most 10000 characters")
    private String description;

    private Long leadUserId;
    private Long categoryId;
    private String url;
    private Long avatarId;
}
