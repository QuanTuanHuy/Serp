/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Update presence status request DTO
 */


package serp.project.discuss_service.core.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.enums.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePresenceStatusRequest {
    @NotNull(message = "Status is required")
    private UserStatus status;

    @Size(max = 255, message = "Status message must not exceed 255 characters")
    private String statusMessage;
}
