/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.OpportunityModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpportunityRepository extends JpaRepository<OpportunityModel, Long> {

    Optional<OpportunityModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<OpportunityModel> findByTenantId(Long tenantId, Pageable pageable);

    Page<OpportunityModel> findByTenantIdAndStage(Long tenantId, String stage, Pageable pageable);

    Page<OpportunityModel> findByTenantIdAndCustomerId(Long tenantId, Long customerId, Pageable pageable);

    List<OpportunityModel> findByTenantIdAndLeadId(Long tenantId, Long leadId);

    Page<OpportunityModel> findByTenantIdAndAssignedTo(Long tenantId, Long assignedTo, Pageable pageable);

    @Query("SELECT o FROM OpportunityModel o WHERE o.tenantId = :tenantId " +
            "AND (LOWER(o.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<OpportunityModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStage(Long tenantId, String stage);

    long countByTenantIdAndCustomerId(Long tenantId, Long customerId);

    long countByTenantIdAndAssignedTo(Long tenantId, Long assignedTo);

    @Query("SELECT COALESCE(SUM(o.estimatedValue), 0) FROM OpportunityModel o WHERE o.tenantId = :tenantId")
    BigDecimal sumEstimatedValueByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COALESCE(SUM(o.estimatedValue), 0) FROM OpportunityModel o " +
            "WHERE o.tenantId = :tenantId AND o.stage = :stage")
    BigDecimal sumEstimatedValueByTenantIdAndStage(@Param("tenantId") Long tenantId,
            @Param("stage") String stage);
}
