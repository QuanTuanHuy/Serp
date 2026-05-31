package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolPickupPointRepository extends BaseRepository<SchoolPickupPointEntity, Long> {

    Page<SchoolPickupPointEntity> findBySchoolIdAndTenantIdAndIsDeletedFalse(
            Long schoolId, Long tenantId, Pageable pageable);

    List<SchoolPickupPointEntity> findBySchoolIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(
            Long schoolId, Long tenantId);

    Optional<SchoolPickupPointEntity> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);

    Optional<SchoolPickupPointEntity> findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
            Long schoolId, Long pickupPointId, Long tenantId);

    boolean existsBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(
            Long schoolId, Long pickupPointId, Long tenantId);

    List<SchoolPickupPointEntity> findByTenantIdAndIsDeletedFalseAndIsActiveTrue(Long tenantId);
}
