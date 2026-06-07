package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;
import java.util.Optional;

public interface SchoolBusUserRepository extends BaseRepository<SchoolBusUserEntity, Long> {

    Optional<SchoolBusUserEntity> findByAccountUserIdAndIsDeletedFalse(Long accountUserId);

    Optional<SchoolBusUserEntity> findByKeycloakIdAndIsDeletedFalse(String keycloakId);

    Optional<SchoolBusUserEntity> findByTenantIdAndEmailIgnoreCaseAndIsDeletedFalse(Long tenantId, String email);

    boolean existsByAccountUserIdAndIsDeletedFalse(Long accountUserId);

}
