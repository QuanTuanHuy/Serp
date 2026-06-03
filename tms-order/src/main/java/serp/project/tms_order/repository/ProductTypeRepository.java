/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.repository.projection.CodeNameProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    Optional<ProductType> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
            select p.code as code, p.name as name
            from ProductType p
            where p.isActive = true
                and (:tenantId is null or p.tenantId = :tenantId)
            order by p.name asc
            """)
    List<CodeNameProjection> findTemplateCodeNameList(@Param("tenantId") Long tenantId);

    List<ProductType> findByTenantIdAndIsActiveTrueOrderByNameAsc(Long tenantId);
}
