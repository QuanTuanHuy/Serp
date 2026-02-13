/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.UserDepartmentEntity;
import serp.project.account.core.port.store.IUserDepartmentPort;
import serp.project.account.infrastructure.store.mapper.UserDepartmentMapper;
import serp.project.account.infrastructure.store.repository.IUserDepartmentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDepartmentAdapter implements IUserDepartmentPort {
    private final IUserDepartmentRepository userDepartmentRepository;
    private final UserDepartmentMapper userDepartmentMapper;

    @Override
    public UserDepartmentEntity save(UserDepartmentEntity userDepartment) {
        var model = userDepartmentMapper.toModel(userDepartment);
        return userDepartmentMapper.toEntity(userDepartmentRepository.save(model));
    }

    @Override
    public Optional<UserDepartmentEntity> getByUserIdAndDepartmentId(Long userId, Long departmentId) {
        return userDepartmentRepository.findByUserIdAndDepartmentId(userId, departmentId)
                .map(userDepartmentMapper::toEntity);
    }

    @Override
    public List<UserDepartmentEntity> getByUserId(Long userId) {
        return userDepartmentMapper.toEntityList(userDepartmentRepository.findByUserId(userId));
    }

    @Override
    public List<UserDepartmentEntity> getActiveByUserId(Long userId) {
        return userDepartmentMapper.toEntityList(
                userDepartmentRepository.findByUserIdAndIsActive(userId, true));
    }

    @Override
    public List<UserDepartmentEntity> getByDepartmentId(Long departmentId) {
        return userDepartmentMapper.toEntityList(userDepartmentRepository.findByDepartmentId(departmentId));
    }

    @Override
    public List<UserDepartmentEntity> getActiveByDepartmentId(Long departmentId) {
        return userDepartmentMapper
                .toEntityList(userDepartmentRepository.findByDepartmentIdAndIsActive(departmentId, true));
    }

    @Override
    public Optional<UserDepartmentEntity> getPrimaryByUserId(Long userId) {
        return userDepartmentRepository.findByUserIdAndIsPrimaryAndIsActive(userId, true, true)
                .map(userDepartmentMapper::toEntity);
    }

    @Override
    public Long countByDepartmentId(Long departmentId) {
        return userDepartmentRepository.countByDepartmentId(departmentId);
    }

    @Override
    public Long countActiveByDepartmentId(Long departmentId) {
        return userDepartmentRepository.countByDepartmentIdAndIsActive(departmentId, true);
    }

    @Override
    public Map<Long, Long> countByDepartmentIds(List<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Map.of();
        }
        return userDepartmentRepository.countByDepartmentIds(departmentIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public Boolean existsByUserIdAndDepartmentId(Long userId, Long departmentId) {
        return userDepartmentRepository.existsByUserIdAndDepartmentId(userId, departmentId);
    }

    @Override
    public void delete(Long userId, Long departmentId) {
        userDepartmentRepository.findByUserIdAndDepartmentId(userId, departmentId).ifPresent(model -> {
            model.setIsActive(false);
            userDepartmentRepository.save(model);
        });
    }

    @Override
    public Long countByOrganizationId(Long organizationId) {
        return userDepartmentRepository.countByOrganizationId(organizationId);
    }

    @Override
    public Long countActiveByOrganizationId(Long organizationId) {
        return userDepartmentRepository.countByOrganizationIdAndIsActive(organizationId, true);
    }
}
