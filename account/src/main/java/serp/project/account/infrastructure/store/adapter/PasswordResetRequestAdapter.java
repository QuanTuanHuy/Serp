/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.PasswordResetRequestEntity;
import serp.project.account.core.domain.enums.ResetPassStatus;
import serp.project.account.core.port.store.IPasswordResetRequestPort;
import serp.project.account.infrastructure.store.mapper.PasswordResetRequestMapper;
import serp.project.account.infrastructure.store.repository.IPasswordResetRequestRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetRequestAdapter implements IPasswordResetRequestPort {
    private final IPasswordResetRequestRepository repository;
    private final PasswordResetRequestMapper mapper;

    @Override
    public Optional<PasswordResetRequestEntity> getByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(mapper::toEntity);
    }

    @Override
    public void invalidatePendingByUserId(Long userId) {
        var pendingRequests = repository.findByUserIdAndStatus(userId, ResetPassStatus.PENDING);
        if (pendingRequests.isEmpty()) {
            return;
        }

        pendingRequests.forEach(request -> request.setStatus(ResetPassStatus.CANCELLED));
        repository.saveAll(pendingRequests);
    }

    @Override
    public PasswordResetRequestEntity save(PasswordResetRequestEntity entity) {
        return mapper.toEntity(repository.save(mapper.toModel(entity)));
    }
}
