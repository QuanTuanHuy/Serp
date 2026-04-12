/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.PriorityModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPriorityRepository extends JpaRepository<PriorityModel, Long> {

    Optional<PriorityModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT p FROM PriorityModel p WHERE p.id = :id AND (p.tenantId = :tenantId OR p.tenantId = 0)")
    Optional<PriorityModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<PriorityModel> findFirstByTenantIdAndPriorityKeyOrderByIdAsc(Long tenantId, String priorityKey);

    List<PriorityModel> findAllByTenantIdOrderBySequenceAsc(Long tenantId);

    @Query("SELECT p FROM PriorityModel p WHERE p.tenantId = :tenantId OR p.tenantId = 0 ORDER BY CASE WHEN p.tenantId = :tenantId THEN 0 ELSE 1 END, p.sequence ASC, p.name ASC, p.id ASC")
    List<PriorityModel> findAllByTenantIdOrSystemTenant(@Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT p
            FROM PriorityModel p
            WHERE (p.tenantId = :tenantId OR p.tenantId = 0)
              AND (:isSystem IS NULL OR p.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(p.priorityKey) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(p)
            FROM PriorityModel p
            WHERE (p.tenantId = :tenantId OR p.tenantId = 0)
              AND (:isSystem IS NULL OR p.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(p.priorityKey) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<PriorityModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                  @Param("search") String search,
                                                  @Param("isSystem") Boolean isSystem,
                                                  Pageable pageable);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);

    @Modifying
    @Query("UPDATE PriorityModel p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
