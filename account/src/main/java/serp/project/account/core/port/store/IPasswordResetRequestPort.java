/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.Optional;

import serp.project.account.core.domain.entity.PasswordResetRequestEntity;

public interface IPasswordResetRequestPort {
    Optional<PasswordResetRequestEntity> getByTokenHash(String tokenHash);

    void invalidatePendingByUserId(Long userId);

    PasswordResetRequestEntity save(PasswordResetRequestEntity entity);
}
