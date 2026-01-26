/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.domain.event;

import java.time.Instant;

public record UserOnlineInternalEvent(
    Long userId,
    String email,
    Instant lastLoginAt
) {
    public UserOnlineInternalEvent {
        if (userId == null && email == null) {
            throw new IllegalArgumentException("User ID or email must be provided");
        }
        if (lastLoginAt == null) {
            lastLoginAt = Instant.now();
        }
    }
}
