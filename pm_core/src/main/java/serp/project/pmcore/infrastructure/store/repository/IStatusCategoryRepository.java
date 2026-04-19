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
import serp.project.pmcore.infrastructure.store.model.StatusCategoryModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStatusCategoryRepository extends JpaRepository<StatusCategoryModel, Long> {

    Optional<StatusCategoryModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT c FROM StatusCategoryModel c WHERE c.id = :id AND (c.tenantId = :tenantId OR c.tenantId = 0)")
    Optional<StatusCategoryModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<StatusCategoryModel> findFirstByTenantIdAndKeyOrderByIdAsc(Long tenantId, String key);

    @Query("SELECT c FROM StatusCategoryModel c WHERE c.key = :key AND (c.tenantId = :tenantId OR c.tenantId = 0)")
    List<StatusCategoryModel> findByKeyAndTenantIdOrSystemTenant(@Param("key") String key, @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT c
            FROM StatusCategoryModel c
            WHERE (c.tenantId = :tenantId OR c.tenantId = 0)
              AND (:isSystem IS NULL OR c.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.key) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(c)
            FROM StatusCategoryModel c
            WHERE (c.tenantId = :tenantId OR c.tenantId = 0)
              AND (:isSystem IS NULL OR c.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.key) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<StatusCategoryModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                        @Param("search") String search,
                                                        @Param("isSystem") Boolean isSystem,
                                                        Pageable pageable);

    boolean existsByTenantIdAndKeyIgnoreCase(Long tenantId, String key);
}
