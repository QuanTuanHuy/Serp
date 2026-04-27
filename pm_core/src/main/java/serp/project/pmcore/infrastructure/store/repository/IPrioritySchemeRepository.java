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
import serp.project.pmcore.infrastructure.store.model.PrioritySchemeModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPrioritySchemeRepository extends JpaRepository<PrioritySchemeModel, Long> {

    Optional<PrioritySchemeModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM PrioritySchemeModel s WHERE s.id = :id AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<PrioritySchemeModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<PrioritySchemeModel> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    @Query(value = """
    SELECT *
    FROM priority_schemes s
    WHERE (s.tenant_id = :tenantId OR s.tenant_id = 0)
      AND s.deleted_at IS NULL
      AND (:search IS NULL OR s.name ILIKE CONCAT('%', :search, '%'))
      AND (
            :isSystem IS NULL
            OR (:isSystem = true AND s.tenant_id = 0)
            OR (:isSystem = false AND s.tenant_id = :tenantId)
          )
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM priority_schemes s
    WHERE (s.tenant_id = :tenantId OR s.tenant_id = 0)
      AND s.deleted_at IS NULL
      AND (:search IS NULL OR s.name ILIKE CONCAT('%', :search, '%'))
      AND (
            :isSystem IS NULL
            OR (:isSystem = true AND s.tenant_id = 0)
            OR (:isSystem = false AND s.tenant_id = :tenantId)
          )
    """,
            nativeQuery = true)
    Page<PrioritySchemeModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                        @Param("search") String search,
                                                        @Param("isSystem") Boolean isSystem,
                                                        Pageable pageable);

    boolean existsByTenantIdAndName(Long tenantId, String name);

    boolean existsByDefaultPriorityIdAndTenantId(Long defaultPriorityId, Long tenantId);

    @Modifying
    @Query("UPDATE PrioritySchemeModel p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
