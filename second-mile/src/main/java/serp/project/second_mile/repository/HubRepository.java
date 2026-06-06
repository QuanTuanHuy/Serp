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
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.repository.projection.CodeNameProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface HubRepository extends JpaRepository<Hub, Long>, JpaSpecificationExecutor<Hub> {
    boolean existsByCode(String code);

    @Query("""
            select h.code as code, h.name as name
            from Hub h
            where h.tenantId = :tenantId
            order by h.name asc
            """)
    List<CodeNameProjection> findTemplateCodeNameListByTenantId(@Param("tenantId") Long tenantId);

    List<Hub> findAllByTenantId(Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select h
            from Hub h
            where h.id = :id
                and h.tenantId = :tenantId
            """)
    Optional<Hub> findByIdAndTenantIdForUpdate(
            @Param("id") Long id,
            @Param("tenantId") Long tenantId
    );
}
