/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.AssignUserToDepartmentRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersToDepartmentRequest;
import serp.project.account.core.domain.entity.UserDepartmentEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IUserDepartmentPort;
import serp.project.account.core.service.IUserDepartmentService;
import serp.project.account.infrastructure.store.mapper.UserDepartmentMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDepartmentService implements IUserDepartmentService {
    private final IUserDepartmentPort userDepartmentPort;
    private final UserDepartmentMapper userDepartmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDepartmentEntity assignUserToDepartment(AssignUserToDepartmentRequest request) {
        long userId = request.getUserId();
        long departmentId = request.getDepartmentId();
        userDepartmentPort.getByUserIdAndDepartmentId(userId, departmentId).ifPresent(ud -> {
            log.error("User {} is already assigned to Department {}", userId, departmentId);
            throw new AppException(Constants.ErrorMessage.USER_ALREADY_IN_DEPARTMENT);
        });

        if (request.getIsPrimary()) {
            userDepartmentPort.getPrimaryByUserId(userId).ifPresent(primaryUd -> {
                primaryUd.setIsPrimary(false);
                userDepartmentPort.save(primaryUd);
            });
        }

        var userDepartment = userDepartmentMapper.createMapper(request);
        return userDepartmentPort.save(userDepartment);
    }

    @Override
    public List<UserDepartmentEntity> bulkAssignUsersToDepartment(BulkAssignUsersToDepartmentRequest request) {
        List<UserDepartmentEntity> assignedEntities = new ArrayList<>();
        for (Long userId : request.getUserIds()) {
            AssignUserToDepartmentRequest assignRequest = AssignUserToDepartmentRequest.builder()
                    .userId(userId)
                    .departmentId(request.getDepartmentId())
                    .jobTitle(request.getJobTitle())
                    .isPrimary(false)
                    .build();
            try {
                UserDepartmentEntity entity = assignUserToDepartment(assignRequest);
                assignedEntities.add(entity);
            } catch (AppException e) {
                log.error("Skipping user {}: {}", userId, e.getMessage());
            }
        }
        return assignedEntities;
    }

    @Override
    public void removeUserFromDepartment(Long userId, Long departmentId) {
        userDepartmentPort.delete(userId, departmentId);
    }

    @Override
    public List<UserDepartmentEntity> getUserDepartmentsByUserId(Long userId) {
        return userDepartmentPort.getByUserId(userId);
    }

    @Override
    public List<UserDepartmentEntity> getDepartmentMembers(Long departmentId) {
        return userDepartmentPort.getByDepartmentId(departmentId);
    }

    @Override
    public List<UserDepartmentEntity> getActiveDepartmentMembers(Long departmentId) {
        return userDepartmentPort.getActiveByDepartmentId(departmentId);
    }

    @Override
    public Long countMembersByDepartmentId(Long departmentId) {
        return userDepartmentPort.countByDepartmentId(departmentId);
    }

}
