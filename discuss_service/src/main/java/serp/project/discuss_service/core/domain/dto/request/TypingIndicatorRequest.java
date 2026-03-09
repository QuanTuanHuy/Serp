/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TypingIndicatorRequest {
    
    @NotNull(message = "Typing status is required")
    private Boolean isTyping;
}
