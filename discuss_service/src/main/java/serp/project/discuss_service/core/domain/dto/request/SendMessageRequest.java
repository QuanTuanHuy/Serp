/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Request DTO for sending messages
 */

package serp.project.discuss_service.core.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for sending a message to a channel.
 * 
 * For text-only messages via REST API (POST /messages):
 * - content is required
 * - parentId can be set for replies
 * 
 * For messages with files (POST /messages/with-files):
 * - content is optional (can send file-only messages)
 * - Files are sent as multipart form data
 * 
 * Validation:
 * - For REST API: content is required (validated at controller level)
 * - For multipart: content OR files required (validated at service level)
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SendMessageRequest {
    
    /**
     * Message content (text).
     * Required for text-only messages, optional when files are attached.
     */
    @Size(max = 10000, message = "Message content must not exceed 10000 characters")
    private String content;
    
    /**
     * List of user IDs mentioned in this message (@mentions)
     */
    private List<Long> mentions;
    
    /**
     * Parent message ID for replies (threading).
     * If set, this message becomes a reply to the parent message.
     */
    private Long parentId;
    
    /**
     * Check if message has valid content
     */
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }
}
