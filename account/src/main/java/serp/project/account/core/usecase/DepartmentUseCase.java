package serp.project.account.core.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.AssignUserToDepartmentRequest;
import serp.project.account.core.domain.dto.request.AssignUserToModuleRequest;
import serp.project.account.core.domain.dto.request.CreateDepartmentRequest;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.UpdateDepartmentRequest;
import serp.project.account.core.domain.dto.response.DepartmentResponse;
import serp.project.account.core.domain.dto.response.UserDepartmentResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserDepartmentService;
import serp.project.account.core.service.IUserModuleAccessService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentUseCase {
    private final IDepartmentService departmentService;
    private final IUserDepartmentService userDepartmentService;
    private final IUserService userService;
    private final IUserModuleAccessService userModuleAccessService;
    private final IOrganizationService organizationService;

    private final ModuleAccessUseCase moduleAccessUseCase;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createDepartment(Long organizationId, CreateDepartmentRequest request) {
        try {
            if (!request.getDefaultModuleIds().isEmpty()) {
                validateModuleAccessForOrganization(organizationId, request.getDefaultModuleIds());
            }
            var department = departmentService.createDepartment(organizationId, request);

            if (department.getManagerId() != null) {
                var assignRequest = AssignUserToDepartmentRequest.builder()
                        .userId(department.getManagerId())
                        .departmentId(department.getId())
                        .jobTitle("Manager")
                        .build();
                userDepartmentService.assignUserToDepartment(assignRequest);
            }

            return responseUtils.success(department);
        } catch (AppException e) {
            log.error("Error creating department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating department: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateDepartment(Long departmentId, UpdateDepartmentRequest request) {
        try {
            var department = departmentService.getDepartmentById(departmentId);
            if (!CollectionUtils.isEmpty(request.getDefaultModuleIds())) {
                validateModuleAccessForOrganization(department.getOrganizationId(), request.getDefaultModuleIds());
            }

            return responseUtils.success(departmentService.updateDepartment(departmentId, request));
        } catch (AppException e) {
            log.error("Error updating department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating department: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> getDepartmentById(Long departmentId) {
        try {
            var department = departmentService.getDepartmentById(departmentId);
            var manager = department.getManagerId() != null
                    ? userService.getUserById(department.getManagerId())
                    : null;
            var parentDepartment = department.getParentDepartmentId() != null
                    ? departmentService.getDepartmentById(department.getParentDepartmentId())
                    : null;
            var response = new DepartmentResponse(
                    department,
                    parentDepartment != null ? parentDepartment.getName() : "",
                    manager != null ? manager.getFullName() : "",
                    0,
                    userDepartmentService.countMembersByDepartmentId(department.getId()).intValue());
            return responseUtils.success(response);

        } catch (AppException e) {
            log.error("Error getting department by id: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting department by id: {}", e.getMessage(), e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteDepartment(Long departmentId) {
        try {
            departmentService.deleteDepartment(departmentId);
            return responseUtils.success("Department deleted successfully");
        } catch (AppException e) {
            log.error("Error deleting department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting department: {}", e.getMessage(), e);
            throw e;
        }
    }

    public GeneralResponse<?> getDepartmentsByOrganization(GetDepartmentParams params) {
        try {
            var departmentPair = departmentService.getDepartments(params);
            var departments = departmentPair.getFirst();
            if (departments.isEmpty()) {
                return responseUtils
                        .success(paginationUtils.getResponse(0L, params.getPage(), params.getPageSize(), departments));
            }

            List<Long> allDepartmentIds = new ArrayList<>();
            departments.forEach(d -> {
                allDepartmentIds.add(d.getId());
                if (d.getParentDepartmentId() != null) {
                    allDepartmentIds.add(d.getParentDepartmentId());
                }
            });
            final List<Long> distinctDepartmentIds = allDepartmentIds.stream().distinct().toList();
            var idToDepartment = departmentService.getDepartmentsByIds(distinctDepartmentIds).stream()
                    .collect(Collectors.toMap(DepartmentEntity::getId, Function.identity()));

            List<Long> userIds = departments.stream().map(DepartmentEntity::getManagerId)
                    .filter(Objects::nonNull)
                    .distinct().toList();
            var idToUser = userService.getUsersByIds(userIds).stream()
                    .collect(Collectors.toMap(u -> u.getId(), Function.identity()));

            var departmentResponses = departments.stream()
                    .map(d -> {
                        var parentDepartment = d.getParentDepartmentId() != null
                                ? idToDepartment.get(d.getParentDepartmentId())
                                : null;
                        var manager = d.getManagerId() != null ? idToUser.get(d.getManagerId()) : null;
                        return new DepartmentResponse(
                                d,
                                parentDepartment != null ? parentDepartment.getName() : null,
                                manager != null ? manager.getFullName() : null,
                                0, // implement later
                                userDepartmentService.countMembersByDepartmentId(d.getId()).intValue());
                    })
                    .toList();
            return responseUtils.success(
                    paginationUtils.getResponse(departmentPair.getSecond(), params.getPage(), params.getPageSize(),
                            departmentResponses));

        } catch (Exception e) {
            log.error("Unexpected error getting departments: {}", e.getMessage(), e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignUserToDepartment(AssignUserToDepartmentRequest request) {
        try {
            var department = departmentService.getDepartmentById(request.getDepartmentId());
            if (!department.isActiveDepartment()) {
                log.error("Cannot assign user to inactive department {}", request.getDepartmentId());
                throw new AppException(Constants.ErrorMessage.DEPARTMENT_INACTIVE);
            }
            var user = userService.getUserById(request.getUserId());
            if (!user.isActive()) {
                log.error("Cannot assign inactive user {} to department {}", request.getUserId(),
                        request.getDepartmentId());
                throw new AppException(Constants.ErrorMessage.USER_INACTIVE);
            }
            if (!department.getOrganizationId().equals(user.getPrimaryOrganizationId())) {
                log.error("User {} does not belong to organization {}", request.getUserId(),
                        department.getOrganizationId());
                throw new AppException(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION);
            }

            userDepartmentService.assignUserToDepartment(request);

            var organization = organizationService.getOrganizationById(department.getOrganizationId());
            List<Long> moduleIdsToAssign = getDefaultModuleIdsForDepartment(department, organization);
            if (!moduleIdsToAssign.isEmpty()) {
                for (long moduleId : moduleIdsToAssign) {
                    try {
                        var assignModuleRequest = AssignUserToModuleRequest.builder()
                                .userId(user.getId())
                                .moduleId(moduleId)
                                .build();
                        moduleAccessUseCase.assignUserToModule(organization.getId(), assignModuleRequest,
                                organization.getOwnerId());
                    } catch (Exception e) {
                        log.error("Error assigning module {} to user {}: {}", moduleId, user.getId(), e.getMessage());
                    }
                }
            }

            return responseUtils.success("User assigned to department successfully");
        } catch (AppException e) {
            log.error("Error assigning user to department: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error assigning user to department: {}", e.getMessage(), e);
            throw e;
        }
    }

    public GeneralResponse<?> getMembersByDepartmentId(Long departmentId) {
        try {
            var members = userDepartmentService.getDepartmentMembers(departmentId);
            var userIds = members.stream().map(ud -> ud.getUserId()).distinct().toList();
            if (userIds.isEmpty()) {
                return responseUtils.success(new ArrayList<UserDepartmentResponse>());
            }
            var idToUser = userService.getUsersByIds(userIds).stream()
                    .collect(Collectors.toMap(u -> u.getId(), Function.identity()));
            var memberResponses = members.stream()
                    .map(ud -> new UserDepartmentResponse(
                            ud,
                            idToUser.get(ud.getUserId())))
                    .toList();
            return responseUtils.success(memberResponses);
        } catch (AppException e) {
            log.error("Error getting members by department id: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting members by department id: {}", e.getMessage(), e);
            return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateModuleAccessForOrganization(Long organizationId, List<Long> moduleIds) {
        var organization = organizationService.getOrganizationById(organizationId);
        long ownerId = organization.getOwnerId();

        var availableModuleIds = userModuleAccessService.getUserModuleAccesses(ownerId, organizationId).stream()
                .filter(uma -> uma.getIsActive())
                .map(uma -> uma.getModuleId())
                .toList();
        for (Long moduleId : moduleIds) {
            if (!availableModuleIds.contains(moduleId)) {
                log.error("Organzation {} does not have access to module {}", organizationId, moduleId);
                throw new AppException(
                        String.format(Constants.ErrorMessage.ORGANIZATION_CANNOT_ACCESS_MODULE_ID, moduleId));
            }
        }
    }

    private List<Long> getDefaultModuleIdsForDepartment(DepartmentEntity department, OrganizationEntity organization) {
        if (CollectionUtils.isEmpty(department.getDefaultModuleIds())) {
            return List.of();
        }
        var nowAvailableModuleIds = userModuleAccessService.getUserModuleAccesses(
                organization.getOwnerId(), organization.getId()).stream()
                .filter(uma -> uma.getIsActive())
                .map(uma -> uma.getModuleId())
                .toList();
        List<Long> validModuleIds = department.getDefaultModuleIds().stream()
                .filter(nowAvailableModuleIds::contains)
                .toList();
        return validModuleIds;
    }
}
