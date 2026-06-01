/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOfficeHandoverManifest;
import serp.project.first_mile.enums.HandoverManifestStatus;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface PostOfficeHandoverManifestRepository extends JpaRepository<PostOfficeHandoverManifest, Long> {
    boolean existsByTenantIdAndManifestCodeIgnoreCase(Long tenantId, String manifestCode);

    Optional<PostOfficeHandoverManifest> findByTenantIdAndManifestCodeIgnoreCase(Long tenantId, String manifestCode);

    Optional<PostOfficeHandoverManifest> findByIdAndTenantId(Long id, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select manifest
            from PostOfficeHandoverManifest manifest
            where manifest.id = :id
                and manifest.tenantId = :tenantId
            """)
    Optional<PostOfficeHandoverManifest> findByIdAndTenantIdForUpdate(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId
    );

    @Query("""
            select manifest
            from PostOfficeHandoverManifest manifest
            where manifest.tenantId = :tenantId
                and (:postOfficeId is null or manifest.originPostOfficeId = :postOfficeId)
                and (:targetHubId is null or manifest.targetHubId = :targetHubId)
                and (:status is null or manifest.status = :status)
            """)
    Page<PostOfficeHandoverManifest> findPage(
            @Param("tenantId") Long tenantId,
            @Param("postOfficeId") Long postOfficeId,
            @Param("targetHubId") Long targetHubId,
            @Param("status") HandoverManifestStatus status,
            Pageable pageable
    );

    @Query("""
            select manifest
            from PostOfficeHandoverManifest manifest
            where manifest.tenantId = :tenantId
                and manifest.originPostOfficeId in :postOfficeIds
                and (:targetHubId is null or manifest.targetHubId = :targetHubId)
                and (:status is null or manifest.status = :status)
            """)
    Page<PostOfficeHandoverManifest> findPageInPostOffices(
            @Param("tenantId") Long tenantId,
            @Param("postOfficeIds") Collection<Long> postOfficeIds,
            @Param("targetHubId") Long targetHubId,
            @Param("status") HandoverManifestStatus status,
            Pageable pageable
    );
}
