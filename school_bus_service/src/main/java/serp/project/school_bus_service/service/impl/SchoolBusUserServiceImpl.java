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

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SchoolBusUserServiceImpl extends AbstractBaseService<SchoolBusUserEntity, Long> implements ISchoolBusUserService {

    private final SchoolBusUserRepository schoolBusUserRepository;
    private final SchoolBusUserMapper schoolBusUserMapper;

    public SchoolBusUserServiceImpl(SchoolBusUserRepository schoolBusUserRepository,
                                    SchoolBusUserMapper schoolBusUserMapper) {
        this.schoolBusUserRepository = schoolBusUserRepository;
        this.schoolBusUserMapper = schoolBusUserMapper;
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

        // LƯU Ý CHO CÁC PHASE SAU:
        // - Tại sao KHÔNG tự động tạo các profile (Parent, Driver, Attendant) ở đây?
        //   Bởi vì việc tạo profile nghiệp vụ đòi hỏi các thông tin nghiệp vụ chuyên biệt (ví dụ: bằng lái xe, 
        //   chứng chỉ giám hộ, hoặc phân luồng trường học) mà sự kiện Kafka User từ Account module không cung cấp.
        //   Việc auto-create profile sẽ được xử lý độc lập ở các phase sau.
        // - Tại sao KHÔNG gọi trực tiếp Account API ở đây?
        //   Đây là hàm nhận dữ liệu từ Kafka consumer hoặc Sync Job để ghi/cache dữ liệu shadow. 
        //   Việc gọi API đồng bộ hoặc fallback sẽ được cấu hình trong REST Client ở Phase 4.
        
        return schoolBusUserRepository.save(entity);
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

}
