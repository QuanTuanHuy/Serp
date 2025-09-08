/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
package serp.project.account.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RevokeTokenRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
