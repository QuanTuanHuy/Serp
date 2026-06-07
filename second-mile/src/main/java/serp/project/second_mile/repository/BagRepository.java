/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BagRepository extends JpaRepository<Bag, Long>, JpaSpecificationExecutor<Bag> {
    boolean existsByTenantIdAndBagCodeIgnoreCase(Long tenantId, String bagCode);

    List<Bag> findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationHubIdAndStatus(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            BagStatus status
    );

    List<Bag> findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationPostOfficeCodeIgnoreCaseAndStatus(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            String destinationPostOfficeCode,
            BagStatus status
    );

    List<Bag> findByTenantIdAndOriginHubIdAndStatusAndSealedAtBetween(
            Long tenantId,
            Long originHubId,
            BagStatus status,
            LocalDateTime from,
            LocalDateTime to
    );
}
