/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.stereotype.Repository;
import serp.project.account.core.domain.enums.ResetPassStatus;
import serp.project.account.infrastructure.store.model.PasswordResetRequestModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPasswordResetRequestRepository extends IBaseRepository<PasswordResetRequestModel> {
    Optional<PasswordResetRequestModel> findByTokenHash(String tokenHash);

    List<PasswordResetRequestModel> findByUserIdAndStatus(Long userId, ResetPassStatus status);
}
