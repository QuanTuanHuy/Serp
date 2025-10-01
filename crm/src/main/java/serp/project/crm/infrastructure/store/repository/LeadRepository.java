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
import serp.project.crm.infrastructure.store.model.LeadModel;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<LeadModel, Long> {

    Optional<LeadModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<LeadModel> findByTenantId(Long tenantId, Pageable pageable);

    Page<LeadModel> findByTenantIdAndLeadStatus(Long tenantId, String leadStatus, Pageable pageable);

    Page<LeadModel> findByTenantIdAndAssignedTo(Long tenantId, Long assignedTo, Pageable pageable);

    Optional<LeadModel> findByEmailAndTenantId(String email, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    @Query("SELECT l FROM LeadModel l WHERE l.tenantId = :tenantId " +
            "AND (LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(l.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(l.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LeadModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    Page<LeadModel> findByTenantIdAndLeadSource(Long tenantId, String leadSource, Pageable pageable);

    Page<LeadModel> findByTenantIdAndIndustry(Long tenantId, String industry, Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndLeadStatus(Long tenantId, String leadStatus);

    long countByTenantIdAndAssignedTo(Long tenantId, Long assignedTo);
}
