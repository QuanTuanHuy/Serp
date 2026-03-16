/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.ResetPassStatus;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class PasswordResetRequestEntity extends BaseEntity {
    private Long userId;
    private Long organizationId;
    private String email;
    private String tokenHash;
    private ResetPassStatus status;
    private Long requestedBy;
    private Long expiresAt;
    private Long usedAt;

    public boolean isPending() {
        return ResetPassStatus.PENDING == status;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().toEpochMilli() > expiresAt;
    }

    public void markUsed() {
        long now = Instant.now().toEpochMilli();
        status = ResetPassStatus.USED;
        usedAt = now;
        setUpdatedAt(now);
    }

    public void markExpired() {
        long now = Instant.now().toEpochMilli();
        status = ResetPassStatus.EXPIRED;
        setUpdatedAt(now);
    }

    public void cancel() {
        status = ResetPassStatus.CANCELLED;
        setUpdatedAt(Instant.now().toEpochMilli());
    }
}
