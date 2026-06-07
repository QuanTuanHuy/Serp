/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.DeliveryManifest;
import serp.project.first_mile.enums.DeliveryManifestStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryManifestRepository extends JpaRepository<DeliveryManifest, Long>,
        JpaSpecificationExecutor<DeliveryManifest> {

    Optional<DeliveryManifest> findByIdAndTenantId(Long id, Long tenantId);

    List<DeliveryManifest> findByTenantIdAndPostOfficeCodeIgnoreCaseAndStatus(
            Long tenantId, String postOfficeCode, DeliveryManifestStatus status);

    List<DeliveryManifest> findByTenantIdAndPostOfficeCodeIgnoreCaseAndPlannedDate(
            Long tenantId, String postOfficeCode, LocalDate plannedDate);

    List<DeliveryManifest> findByTenantIdAndPostOfficeCodeIgnoreCase(
            Long tenantId, String postOfficeCode);

    Optional<DeliveryManifest> findTopByTenantIdAndManifestCodeStartingWithOrderByManifestCodeDesc(
            Long tenantId, String prefix);
}
