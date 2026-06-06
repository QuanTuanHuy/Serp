/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.HandoverManifest;
import serp.project.second_mile.enums.HandoverManifestStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface HandoverManifestRepository extends JpaRepository<HandoverManifest, Long>, JpaSpecificationExecutor<HandoverManifest> {
    boolean existsByTenantIdAndManifestCodeIgnoreCase(Long tenantId, String manifestCode);

    Optional<HandoverManifest> findByTenantIdAndManifestCodeIgnoreCase(Long tenantId, String manifestCode);

    Optional<HandoverManifest> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndVehicleIdAndStatusIn(
            Long tenantId,
            Long vehicleId,
            Collection<HandoverManifestStatus> statuses
    );

    @Query("""
            select count(manifest) > 0
            from HandoverManifest manifest
            where manifest.tenantId = :tenantId
                and manifest.status in :statuses
                and (:excludeManifestId is null or manifest.id <> :excludeManifestId)
                and manifest.plannedDepartureAt is not null
                and manifest.plannedArrivalAt is not null
                and manifest.plannedDepartureAt < :plannedArrivalAt
                and manifest.plannedArrivalAt > :plannedDepartureAt
                and (
                    manifest.vehicleId = :vehicleId
                    or (:assignedDriverId is not null and manifest.assignedDriverId = :assignedDriverId)
                )
            """)
    boolean existsOverlappingActiveAssignment(
            @Param("tenantId") Long tenantId,
            @Param("vehicleId") Long vehicleId,
            @Param("assignedDriverId") Long assignedDriverId,
            @Param("plannedDepartureAt") LocalDateTime plannedDepartureAt,
            @Param("plannedArrivalAt") LocalDateTime plannedArrivalAt,
            @Param("statuses") Collection<HandoverManifestStatus> statuses,
            @Param("excludeManifestId") Long excludeManifestId
    );
}
