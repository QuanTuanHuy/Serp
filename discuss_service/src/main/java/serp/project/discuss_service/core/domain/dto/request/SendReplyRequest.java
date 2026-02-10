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
public class SendReplyRequest {
    
    @NotNull(message = "Parent message ID is required")
    private Long parentId;
    
    @NotBlank(message = "Reply content is required")
    @Size(max = 10000, message = "Reply content must not exceed 10000 characters")
    private String content;

    private List<Long> mentions;
}
