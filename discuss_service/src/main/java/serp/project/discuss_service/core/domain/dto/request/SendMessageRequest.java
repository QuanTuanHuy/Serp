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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SendMessageRequest {
    
    @NotBlank
    @Size(max = 10000, message = "Message content must not exceed 10000 characters")
    private String content;

    private List<Long> mentions;
    
    private Long parentId;
    
}
