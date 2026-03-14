/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.UserInvitationEntity;
import serp.project.account.core.port.store.IUserInvitationPort;
import serp.project.account.infrastructure.store.mapper.UserInvitationMapper;
import serp.project.account.infrastructure.store.repository.IUserInvitationRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserInvitationAdapter implements IUserInvitationPort {
    private final IUserInvitationRepository repository;
    private final UserInvitationMapper mapper;

    @Override
    public UserInvitationEntity save(UserInvitationEntity invitation) {
        var model = mapper.toModel(invitation);
        return mapper.toEntity(repository.save(model));
    }

    @Override
    public Optional<UserInvitationEntity> getByToken(String token) {
        return repository.findByToken(token).map(mapper::toEntity);
    }

    @Override
    public Optional<UserInvitationEntity> getById(Long id, Long organizationId) {
        return repository.findByIdAndOrganizationId(id, organizationId)
                .map(mapper::toEntity);
    }

    @Override
    public Optional<UserInvitationEntity> getPendingByOrgAndEmail(Long organizationId, String email) {
        return repository.findByOrganizationIdAndEmailAndStatus(organizationId, email, "PENDING")
                .map(mapper::toEntity);
    }

    @Override
    public Pair<Long, List<UserInvitationEntity>> getByOrganizationId(Long organizationId, int page, int pageSize) {
        var pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = repository.findByOrganizationId(organizationId, pageable);
        return Pair.of(result.getTotalElements(), mapper.toEntityList(result.getContent()));
    }

    @Override
    public Pair<Long, List<UserInvitationEntity>> getByOrganizationIdAndStatus(
            Long organizationId, String status, int page, int pageSize) {
        var pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = repository.findByOrganizationIdAndStatus(organizationId, status, pageable);
        return Pair.of(result.getTotalElements(), mapper.toEntityList(result.getContent()));
    }

    @Override
    public Integer countPendingByOrganizationId(Long organizationId) {
        return repository.countByOrganizationIdAndStatus(organizationId, "PENDING");
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
