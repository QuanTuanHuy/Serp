/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Bag> findByTenantIdAndOriginHubIdAndStatus(
            Long tenantId,
            Long originHubId,
            BagStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select bag
            from Bag bag
            where bag.tenantId = :tenantId
                and bag.id in :bagIds
            """)
    List<Bag> findByIdInAndTenantIdForUpdate(
            @Param("tenantId") Long tenantId,
            @Param("bagIds") List<Long> bagIds
    );
}
