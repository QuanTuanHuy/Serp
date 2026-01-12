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

/**
 * Request DTO for editing a message
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EditMessageRequest {
    
    @NotBlank(message = "New content is required")
    @Size(max = 10000, message = "Message content must not exceed 10000 characters")
    private String content;
}
