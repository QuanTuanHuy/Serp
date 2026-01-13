/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Request DTO for sending message with file attachments
 */

package serp.project.discuss_service.core.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for sending a message with file attachments.
 * Used as the JSON part of a multipart request.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SendMessageWithFilesRequest {
    
    /**
     * Message content (optional when files are attached)
     */
    @Size(max = 10000, message = "Message content must not exceed 10000 characters")
    private String content;
    
    /**
     * List of user IDs mentioned in this message
     */
    private List<Long> mentions;
    
    /**
     * Parent message ID for replies (optional)
     */
    private Long parentId;
}
