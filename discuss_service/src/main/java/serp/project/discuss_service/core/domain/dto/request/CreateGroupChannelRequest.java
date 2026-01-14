/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a GROUP channel
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateGroupChannelRequest {
    
    @NotBlank(message = "Channel name is required")
    @Size(max = 255, message = "Channel name must not exceed 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Builder.Default
    private Boolean isPrivate = false;
    
    /**
     * List of user IDs to add as initial members
     */
    private List<Long> memberIds;
}
