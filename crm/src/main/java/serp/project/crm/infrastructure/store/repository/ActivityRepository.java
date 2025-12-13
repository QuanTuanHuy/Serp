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
import serp.project.crm.infrastructure.store.model.ActivityModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityModel, Long> {

    Optional<ActivityModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<ActivityModel> findByTenantId(Long tenantId, Pageable pageable);
    Page<ActivityModel> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);
    Page<ActivityModel> findByTenantIdAndActivityType(Long tenantId, String activityType, Pageable pageable);
    Page<ActivityModel> findByTenantIdAndAssignedTo(Long tenantId, Long assignedTo, Pageable pageable);
    Page<ActivityModel> findByTenantIdAndLeadId(Long tenantId, Long leadId, Pageable pageable);
    Page<ActivityModel> findByTenantIdAndCustomerId(Long tenantId, Long customerId, Pageable pageable);
    Page<ActivityModel> findByTenantIdAndOpportunityId(Long tenantId, Long opportunityId, Pageable pageable);
    Page<ActivityModel> findByTenantIdAndContactId(Long tenantId, Long contactId, Pageable pageable);

    @Query("SELECT a FROM ActivityModel a WHERE a.tenantId = :tenantId " +
            "AND (LOWER(a.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ActivityModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT a FROM ActivityModel a WHERE a.tenantId = :tenantId " +
            "AND a.status <> 'COMPLETED' AND a.dueDate < :currentTime")
    Page<ActivityModel> findOverdueActivities(@Param("tenantId") Long tenantId,
            @Param("currentTime") Long currentTime,
            Pageable pageable);

    @Query("SELECT a FROM ActivityModel a WHERE a.tenantId = :tenantId " +
            "AND a.status <> 'COMPLETED' AND a.dueDate BETWEEN :startTime AND :endTime")
    List<ActivityModel> findUpcomingActivities(@Param("tenantId") Long tenantId,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);

    List<ActivityModel> findByLeadId(Long leadId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, String status);

    long countByTenantIdAndAssignedTo(Long tenantId, Long assignedTo);
}
