/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Request DTO for sending messages
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
 * Request DTO for sending a message to a channel.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SendMessageRequest {
    
    /**
     * Message content
     */
    @NotBlank
    @Size(max = 10000, message = "Message content must not exceed 10000 characters")
    private String content;
    
    /**
     * List of user IDs mentioned in this message (@mentions)
     */
    private List<Long> mentions;
    
    /**
     * Parent message ID for replies (threading).
     */
    private Long parentId;
    
}
