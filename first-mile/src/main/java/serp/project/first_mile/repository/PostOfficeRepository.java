/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOffice;

import java.util.Collection;
import java.util.Set;
import java.util.Optional;

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

    Page<PostOffice> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name, Pageable pageable);

    Page<PostOffice> findByLocationIsNull(Pageable pageable);

    Optional<PostOffice> findByIdAndTenantId(Long id, Long tenantId);
}
