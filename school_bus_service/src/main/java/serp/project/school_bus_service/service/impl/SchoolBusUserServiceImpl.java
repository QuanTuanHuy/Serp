package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.SchoolBusUserUpsertCommand;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.mapper.SchoolBusUserMapper;
import serp.project.school_bus_service.repository.SchoolBusUserRepository;
import serp.project.school_bus_service.service.ISchoolBusUserService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import org.springframework.context.annotation.Lazy;
import serp.project.school_bus_service.service.IParentService;
import serp.project.school_bus_service.service.IDriverService;
import serp.project.school_bus_service.service.IAttendantService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SchoolBusUserServiceImpl extends AbstractBaseService<SchoolBusUserEntity, Long> implements ISchoolBusUserService {

    private final SchoolBusUserRepository schoolBusUserRepository;
    private final SchoolBusUserMapper schoolBusUserMapper;
    private final IParentService parentService;
    private final IDriverService driverService;
    private final IAttendantService attendantService;
    private final ObjectMapper objectMapper;

    public SchoolBusUserServiceImpl(SchoolBusUserRepository schoolBusUserRepository,
                                    SchoolBusUserMapper schoolBusUserMapper,
                                    @Lazy IParentService parentService,
                                    @Lazy IDriverService driverService,
                                    @Lazy IAttendantService attendantService,
                                    ObjectMapper objectMapper) {
        this.schoolBusUserRepository = schoolBusUserRepository;
        this.schoolBusUserMapper = schoolBusUserMapper;
        this.parentService = parentService;
        this.driverService = driverService;
        this.attendantService = attendantService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected BaseRepository<SchoolBusUserEntity, Long> getRepository() {
        return schoolBusUserRepository;
    }

    @Override
    @Transactional
    public SchoolBusUserEntity upsertFromAccountUser(SchoolBusUserUpsertCommand command) {
        if (command == null) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, "Command is null");
        }

        // 1. Tìm bản ghi shadow user hiện tại theo thứ tự ưu tiên:
        //    - Ưu tiên 1: Tìm theo accountUserId (ID từ bảng users của Account Module)
        //    - Ưu tiên 2: Tìm theo keycloakId (định danh từ Keycloak phục vụ khớp token)
        //    - Ưu tiên 3: Tìm theo cặp tenantId + email (dành cho các user tạo tay hoặc chưa có keycloakId)
        // Việc khớp theo nhiều định danh giúp tối đa hóa khả năng nhận diện người dùng và tránh bị trùng lặp
        // bản ghi khi thông tin tài khoản được cập nhật từ nhiều nguồn khác nhau.
        SchoolBusUserEntity entity = null;
        if (command.getAccountUserId() != null) {
            entity = schoolBusUserRepository.findByAccountUserIdAndIsDeletedFalse(command.getAccountUserId()).orElse(null);
        }
        if (entity == null && command.getKeycloakId() != null && !command.getKeycloakId().isBlank()) {
            entity = schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(command.getKeycloakId()).orElse(null);
        }
        if (entity == null && command.getTenantId() != null && command.getEmail() != null && !command.getEmail().isBlank()) {
            entity = schoolBusUserRepository.findByTenantIdAndEmailIgnoreCaseAndIsDeletedFalse(command.getTenantId(), command.getEmail()).orElse(null);
        }

        boolean isNew = false;
        if (entity == null) {
            entity = new SchoolBusUserEntity();
            entity.markCreated(command.getTenantId(), actor(null));
            entity.setIsActive(true);
            entity.setIsDeleted(false);
            isNew = true;
        } else {
            entity.markUpdated(actor(null));
        }

        // Áp dụng dữ liệu cập nhật từ Command
        schoolBusUserMapper.applyUpsertCommand(entity, command);
        entity.setLastSyncedAt(LocalDateTime.now());

        // Xử lý logic trạng thái hoạt động dựa trên status từ Account gửi về.
        // - ACTIVE -> isActive = true
        // - INACTIVE, SUSPENDED, DELETED, INVITED -> isActive = false
        // Chúng tôi không thực hiện hard delete hoặc soft delete (isDeleted = true) để bảo toàn dữ liệu.
        if (entity.getStatus() != null) {
            if ("ACTIVE".equalsIgnoreCase(entity.getStatus())) {
                entity.setIsActive(true);
            } else if ("INACTIVE".equalsIgnoreCase(entity.getStatus())
                    || "SUSPENDED".equalsIgnoreCase(entity.getStatus())
                    || "DELETED".equalsIgnoreCase(entity.getStatus())
                    || "INVITED".equalsIgnoreCase(entity.getStatus())) {
                entity.setIsActive(false);
            }
        }

        // Trigger synchronization of business profiles based on roles.
        SchoolBusUserEntity savedUser = schoolBusUserRepository.save(entity);

        List<String> roles = command.getRoles();
        if (roles == null && command.getRawPayloadJson() != null) {
            try {
                roles = new ArrayList<>();
                JsonNode root = objectMapper.readTree(command.getRawPayloadJson());
                JsonNode rolesNode = root.get("roles");
                if (rolesNode == null || rolesNode.isMissingNode()) {
                    rolesNode = root.get("roleNames");
                }
                if (rolesNode != null && rolesNode.isArray()) {
                    for (JsonNode node : rolesNode) {
                        roles.add(node.asText());
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors and fallback to empty
            }
        }

        if (roles != null) {
            boolean isParent = roles.stream().anyMatch("SCHOOL_BUS_PARENT"::equalsIgnoreCase);
            boolean isDriver = roles.stream().anyMatch("SCHOOL_BUS_DRIVER"::equalsIgnoreCase);
            boolean isAttendant = roles.stream().anyMatch("SCHOOL_BUS_ATTENDANT"::equalsIgnoreCase);

            parentService.syncProfile(savedUser, isParent);
            driverService.syncProfile(savedUser, isDriver);
            attendantService.syncProfile(savedUser, isAttendant);
        }

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolBusUserEntity> findByAccountUserId(Long accountUserId) {
        return schoolBusUserRepository.findByAccountUserIdAndIsDeletedFalse(accountUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolBusUserEntity> findByKeycloakId(String keycloakId) {
        return schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolBusUserEntity> findByTenantIdAndEmail(Long tenantId, String email) {
        return schoolBusUserRepository.findByTenantIdAndEmailIgnoreCaseAndIsDeletedFalse(tenantId, email);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolBusUserEntity getRequiredByAccountUserId(Long accountUserId) {
        return findByAccountUserId(accountUserId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "School bus shadow user not found by account user ID: " + accountUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolBusUserEntity getRequiredByKeycloakId(String keycloakId) {
        return findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "School bus shadow user not found by Keycloak ID: " + keycloakId));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<SchoolBusUserEntity> findAllByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return schoolBusUserRepository.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SchoolBusUserEntity> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return schoolBusUserRepository.findById(id)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()));
    }

}
