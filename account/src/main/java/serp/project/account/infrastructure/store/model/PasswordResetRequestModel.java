/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.ResetPassStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_requests", indexes = {
        @Index(name = "idx_password_reset_user_status", columnList = "user_id, status"),
        @Index(name = "idx_password_reset_token_hash", columnList = "token_hash")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class PasswordResetRequestModel extends BaseModel {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResetPassStatus status;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
