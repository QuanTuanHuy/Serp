/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.HandoverManifest;
import serp.project.second_mile.enums.HandoverManifestStatus;

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
}
