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

/**
 * Request DTO for creating or getting a DIRECT channel between two users
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateDirectChannelRequest {
    
    @NotNull(message = "Other user ID is required")
    private Long otherUserId;
}
