/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.repository.projection.CodeNameProjection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PostOfficeRepository extends JpaRepository<PostOffice, Long> {
    boolean existsByCode(String code);

    boolean existsByCodeIgnoreCaseAndTenantId(String code, Long tenantId);

    @Query("""
            select lower(trim(po.code))
            from PostOffice po
            where po.tenantId = :tenantId
                and lower(trim(po.code)) in :normalizedCodes
            """)
    Set<String> findExistingCodesByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("normalizedCodes") Collection<String> normalizedCodes
    );

    List<PostOffice> findAllByTenantId(Long tenantId);

    List<PostOffice> findAllByTenantIdAndIdIn(Long tenantId, Collection<Long> ids);

    Page<PostOffice> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name, Pageable pageable);

    Page<PostOffice> findByLocationIsNull(Pageable pageable);

    @Query("""
            select p.code as code, p.name as name
            from PostOffice p
            where p.tenantId = :tenantId
            order by p.name asc
            """)
    List<CodeNameProjection> findTemplateCodeNameListByTenantId(@Param("tenantId") Long tenantId);

    @Query("""
            select p.code as code, p.name as name
            from PostOffice p
            where p.tenantId = :tenantId
                and p.id in :postOfficeIds
            order by p.name asc
            """)
    List<CodeNameProjection> findTemplateCodeNameListByTenantIdAndIds(
            @Param("tenantId") Long tenantId,
            @Param("postOfficeIds") Collection<Long> postOfficeIds
    );

    Optional<PostOffice> findByIdAndTenantId(Long id, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PostOffice p
            where p.id = :id
                and p.tenantId = :tenantId
            """)
    Optional<PostOffice> findByIdAndTenantIdForUpdate(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId
    );
}
