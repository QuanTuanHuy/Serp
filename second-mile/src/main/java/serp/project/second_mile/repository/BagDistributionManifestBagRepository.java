/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.BagDistributionManifestBag;
import serp.project.second_mile.enums.BagDistributionManifestStatus;

import java.util.Collection;
import java.util.List;

@Repository
public interface BagDistributionManifestBagRepository extends JpaRepository<BagDistributionManifestBag, Long> {
    List<BagDistributionManifestBag> findByManifest_IdAndTenantId(Long manifestId, Long tenantId);

    List<BagDistributionManifestBag> findByManifest_IdAndBagIdInAndTenantId(
            Long manifestId,
            List<Long> bagIds,
            Long tenantId
    );

    @Query("""
            select item.bagId
            from BagDistributionManifestBag item
            join item.manifest manifest
            where item.tenantId = :tenantId
                and item.bagId in :bagIds
                and manifest.status in :statuses
            """)
    List<Long> findActiveBagIds(
            @Param("tenantId") Long tenantId,
            @Param("bagIds") Collection<Long> bagIds,
            @Param("statuses") Collection<BagDistributionManifestStatus> statuses
    );
}
