/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateTopicChannelRequest {
    
    @NotBlank(message = "Channel name is required")
    @Size(max = 255, message = "Channel name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Entity type is required")
    @Size(max = 100, message = "Entity type must not exceed 100 characters")
    private String entityType;
    
    @NotNull(message = "Entity ID is required")
    private Long entityId;
    
    /**
     * List of user IDs to add as initial members
     */
    private List<Long> memberIds;
}
