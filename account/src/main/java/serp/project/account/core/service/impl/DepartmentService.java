/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateDepartmentRequest;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.UpdateDepartmentRequest;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IDepartmentPort;
import serp.project.account.core.port.store.IUserOrganizationPort;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.infrastructure.store.mapper.DepartmentMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService implements IDepartmentService {
    private final IDepartmentPort departmentPort;
    private final IUserOrganizationPort userOrganizationPort;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentEntity createDepartment(Long organizationId, CreateDepartmentRequest request) {

        if (request.getManagerId() != null) {
            validateManager(request.getManagerId(), organizationId);
        }
        if (request.getParentDepartmentId() != null) {
            validateParentDepartment(null, request.getParentDepartmentId(), organizationId);
        }

        var department = departmentMapper.createMapper(request, organizationId);
        return departmentPort.save(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentEntity updateDepartment(Long departmentId, UpdateDepartmentRequest request) {

        var department = getDepartmentById(departmentId);

        if (request.getManagerId() != null) {
            validateManager(request.getManagerId(), department.getOrganizationId());
        }
        if (request.getParentDepartmentId() != null) {
            validateParentDepartment(departmentId, request.getParentDepartmentId(), department.getOrganizationId());
        }
        department = departmentMapper.updateMapper(department, request);

        return departmentPort.save(department);
    }

    @Override
    public DepartmentEntity getDepartmentById(Long departmentId) {
        return departmentPort.getById(departmentId)
                .orElseThrow(() -> {
                    log.error("[DepartmentService] Department not found: {}", departmentId);
                    return new AppException(Constants.ErrorMessage.DEPARTMENT_NOT_FOUND);
                });
    }

    @Override
    public List<DepartmentEntity> getDepartmentsByOrganizationId(Long organizationId) {
        return departmentPort.getByOrganizationId(organizationId);
    }

    @Override
    public List<DepartmentEntity> getActiveDepartmentsByOrganizationId(Long organizationId) {
        return departmentPort.getActiveByOrganizationId(organizationId);
    }

    @Override
    public List<DepartmentEntity> getChildDepartments(Long parentDepartmentId) {
        return departmentPort.getByParentDepartmentId(parentDepartmentId);
    }

    @Override
    public Pair<List<DepartmentEntity>, Long> getDepartments(GetDepartmentParams params) {
        return departmentPort.getDepartments(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long departmentId) {
        getDepartmentById(departmentId);

        var children = departmentPort.getByParentDepartmentId(departmentId);
        if (!children.isEmpty()) {
            log.info("[DepartmentService] Department {} has {} children, setting their parent to null",
                    departmentId, children.size());
            children.forEach(child -> {
                child.setParentDepartmentId(null);
                departmentPort.save(child);
            });
        }

        departmentPort.delete(departmentId);
    }

    @Override
    public void validateManager(Long managerId, Long organizationId) {
        var userOrgs = userOrganizationPort.getByUserId(managerId);
        boolean belongsToOrg = userOrgs.stream()
                .anyMatch(uo -> uo.getOrganizationId().equals(organizationId));

        if (!belongsToOrg) {
            log.error("[DepartmentService] Manager {} does not belong to organization {}",
                    managerId, organizationId);
            throw new AppException(Constants.ErrorMessage.MANAGER_NOT_IN_ORGANIZATION);
        }
    }

    @Override
    public void validateParentDepartment(Long departmentId, Long parentDepartmentId, Long organizationId) {
        var parent = getDepartmentById(parentDepartmentId);

        if (!parent.getOrganizationId().equals(organizationId)) {
            log.error("[DepartmentService] Parent department {} does not belong to organization {}",
                    parentDepartmentId, organizationId);
            throw new AppException(Constants.ErrorMessage.PARENT_DEPARTMENT_NOT_IN_ORGANIZATION);
        }

        if (departmentId != null) {
            checkCircularParent(departmentId, parentDepartmentId, new HashSet<>());
        }
    }

    private void checkCircularParent(Long departmentId, Long parentDepartmentId, Set<Long> visited) {
        if (parentDepartmentId == null) {
            return;
        }
        if (visited.contains(parentDepartmentId) || parentDepartmentId.equals(departmentId)) {
            log.error("[DepartmentService] Circular parent relationship detected");
            throw new AppException(Constants.ErrorMessage.CIRCULAR_PARENT_RELATIONSHIP);
        }

        visited.add(parentDepartmentId);

        var parent = departmentPort.getById(parentDepartmentId).orElse(null);
        if (parent != null && parent.getParentDepartmentId() != null) {
            checkCircularParent(departmentId, parent.getParentDepartmentId(), visited);
        }
    }

    @Override
    public List<DepartmentEntity> getDepartmentsByIds(List<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        return departmentPort.getByIds(departmentIds);
    }
}
