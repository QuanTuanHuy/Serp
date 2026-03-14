/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.infrastructure.store.model.UserModel;
import serp.project.account.infrastructure.store.repository.IUserRepository;
import serp.project.account.infrastructure.store.specification.UserSpecification;
import serp.project.account.kernel.utils.PaginationUtils;

@Component
@RequiredArgsConstructor
public class UserAdapter implements IUserPort {
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final PaginationUtils paginationUtils;

    @Override
    public UserEntity save(UserEntity user) {
        UserModel userModel = userMapper.toModel(user);
        return userMapper.toEntity(userRepository.save(userModel));
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toEntity)
                .orElse(null);
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toEntity)
                .orElse(null);
    }

    @Override
    public Pair<Long, List<UserEntity>> getUsers(GetUserParams params) {
        var pageable = paginationUtils.getPageable(params);
        var specification = UserSpecification.searchUsersWithEmailOrName(params.getSearch())
                .and(UserSpecification.hasOrganizationId(params.getOrganizationId()))
                .and(UserSpecification.hasStatus(params.getStatus()))
                .and(UserSpecification.hasUserType(params.getUserType()))
                .and(UserSpecification.hasRoleId(params.getRoleId()))
                .and(UserSpecification.hasDepartmentId(params.getDepartmentId()));
        var page = userRepository.findAll(specification, pageable);

        var users = userMapper.toEntityList(page.getContent());
        return Pair.of(page.getTotalElements(), users);
    }

    @Override
    public List<UserEntity> getUsersByIds(List<Long> userIds) {
        return userMapper.toEntityList(userRepository.findByIdIn(userIds));
    }

    @Override
    public List<UserEntity> getUsersByOrganizationId(Long organizationId) {
        return userMapper.toEntityList(userRepository.findByPrimaryOrganizationId(organizationId));
    }

    @Override
    public Integer countUsersByOrganizationId(Long organizationId) {
        return userRepository.countByPrimaryOrganizationId(organizationId);
    }

    @Override
    public Integer countUsersByOrganizationIdAndStatus(Long organizationId, UserStatus status) {
        return userRepository.countByPrimaryOrganizationIdAndStatus(organizationId, status);
    }

    @Override
    public Integer countUsersByOrganizationIdAndCreatedBetween(Long organizationId, LocalDateTime from, LocalDateTime to) {
        return userRepository.countByOrganizationIdAndCreatedAtBetween(organizationId, from, to);
    }

    @Override
    public Integer countAdminUsersByOrganizationId(Long organizationId) {
        return userRepository.countAdminUsersByOrganizationId(organizationId);
    }

    @Override
    public List<UserEntity> getUsersByOrganizationIdAndIds(Long organizationId, List<Long> userIds) {
        return userMapper.toEntityList(userRepository.findByPrimaryOrganizationIdAndIdIn(organizationId, userIds));
    }
}
