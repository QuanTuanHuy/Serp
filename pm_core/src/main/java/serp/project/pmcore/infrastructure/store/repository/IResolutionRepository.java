/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ResolutionModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IResolutionRepository extends JpaRepository<ResolutionModel, Long> {

    Optional<ResolutionModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT r FROM ResolutionModel r WHERE r.id = :id AND (r.tenantId = :tenantId OR r.tenantId = 0)")
    Optional<ResolutionModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<ResolutionModel> findFirstByTenantIdAndNameOrderByIdAsc(Long tenantId, String name);

    @Query("SELECT r FROM ResolutionModel r WHERE LOWER(r.name) = LOWER(:name) AND (r.tenantId = :tenantId OR r.tenantId = 0) ORDER BY CASE WHEN r.tenantId = :tenantId THEN 0 ELSE 1 END, r.id ASC")
    List<ResolutionModel> findByNameAndTenantIdOrSystemTenant(@Param("name") String name, @Param("tenantId") Long tenantId);

    List<ResolutionModel> findAllByTenantIdOrderBySequenceAsc(Long tenantId);

    @Query(value = """
            SELECT r
            FROM ResolutionModel r
            WHERE (r.tenantId = :tenantId OR r.tenantId = 0)
              AND (:isSystem IS NULL OR r.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM ResolutionModel r
            WHERE (r.tenantId = :tenantId OR r.tenantId = 0)
              AND (:isSystem IS NULL OR r.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<ResolutionModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                    @Param("search") String search,
                                                    @Param("isSystem") Boolean isSystem,
                                                    Pageable pageable);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}
