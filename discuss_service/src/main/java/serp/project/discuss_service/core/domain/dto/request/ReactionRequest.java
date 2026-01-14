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
 * Request DTO for adding/removing a reaction to/from a message
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReactionRequest {
    
    @NotBlank(message = "Emoji is required")
    @Size(max = 50, message = "Emoji must not exceed 50 characters")
    private String emoji;
}
