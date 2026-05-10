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
import serp.project.pmcore.infrastructure.store.model.StatusModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStatusRepository extends JpaRepository<StatusModel, Long> {

    Optional<StatusModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM StatusModel s WHERE s.id = :id AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<StatusModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<StatusModel> findFirstByTenantIdAndStatusKeyOrderByIdAsc(Long tenantId, String statusKey);

    @Query("SELECT s FROM StatusModel s WHERE s.statusKey = :statusKey AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    List<StatusModel> findByStatusKeyAndTenantIdOrSystemTenant(@Param("statusKey") String statusKey,
                                                                @Param("tenantId") Long tenantId);

    List<StatusModel> findAllByTenantId(Long tenantId);

    List<StatusModel> findAllByIdInAndTenantId(List<Long> statusIds, Long tenantId);

    @Query("SELECT s FROM StatusModel s WHERE s.tenantId = :tenantId OR s.tenantId = 0")
    List<StatusModel> findAllByTenantIdOrSystemTenant(@Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT s
            FROM StatusModel s
            WHERE (s.tenantId = :tenantId OR s.tenantId = 0)
              AND (:statusCategoryId IS NULL OR s.categoryId = :statusCategoryId)
              AND (:isSystem IS NULL OR s.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(s.statusKey) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(s)
            FROM StatusModel s
            WHERE (s.tenantId = :tenantId OR s.tenantId = 0)
              AND (:statusCategoryId IS NULL OR s.categoryId = :statusCategoryId)
              AND (:isSystem IS NULL OR s.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(s.statusKey) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<StatusModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                @Param("search") String search,
                                                @Param("statusCategoryId") Long statusCategoryId,
                                                @Param("isSystem") Boolean isSystem,
                                                Pageable pageable);

    boolean existsByCategoryIdAndTenantId(Long categoryId, Long tenantId);

    boolean existsByTenantIdAndStatusKeyIgnoreCase(Long tenantId, String statusKey);
}
