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
import serp.project.pmcore.infrastructure.store.model.IssueTypeModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIssueTypeRepository extends JpaRepository<IssueTypeModel, Long> {

    Optional<IssueTypeModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT i FROM IssueTypeModel i WHERE i.id = :id AND (i.tenantId = :tenantId OR i.tenantId = 0)")
    Optional<IssueTypeModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<IssueTypeModel> findFirstByTenantIdAndTypeKeyOrderByIdAsc(Long tenantId, String typeKey);

    @Query("SELECT i FROM IssueTypeModel i WHERE i.typeKey = :typeKey AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY CASE WHEN i.tenantId = :tenantId THEN 0 ELSE 1 END, i.id ASC")
    List<IssueTypeModel> findByTypeKeyAndTenantIdOrSystemTenant(@Param("typeKey") String typeKey,
                                                                 @Param("tenantId") Long tenantId);

    List<IssueTypeModel> findAllByTenantIdOrderByHierarchyLevelAsc(Long tenantId);

    @Query("SELECT i FROM IssueTypeModel i WHERE i.tenantId = :tenantId OR i.tenantId = 0 ORDER BY CASE WHEN i.tenantId = :tenantId THEN 0 ELSE 1 END, i.hierarchyLevel ASC, i.name ASC, i.id ASC")
    List<IssueTypeModel> findAllByTenantIdOrSystemTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT i FROM IssueTypeModel i WHERE i.id IN :issueTypeIds AND (i.tenantId = :tenantId OR i.tenantId = 0)")
    List<IssueTypeModel> findAllByIdInAndTenantIdOrSystemTenant(@Param("issueTypeIds") List<Long> issueTypeIds,
                                                                @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT i
            FROM IssueTypeModel i
            WHERE (i.tenantId = :tenantId OR i.tenantId = 0)
              AND (:hierarchyLevel IS NULL OR i.hierarchyLevel = :hierarchyLevel)
              AND (:isSystem IS NULL OR i.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(i.typeKey) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(i)
            FROM IssueTypeModel i
            WHERE (i.tenantId = :tenantId OR i.tenantId = 0)
              AND (:hierarchyLevel IS NULL OR i.hierarchyLevel = :hierarchyLevel)
              AND (:isSystem IS NULL OR i.isSystem = :isSystem)
              AND (
                    :search IS NULL
                    OR LOWER(i.typeKey) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<IssueTypeModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                   @Param("search") String search,
                                                   @Param("hierarchyLevel") Integer hierarchyLevel,
                                                   @Param("isSystem") Boolean isSystem,
                                                   Pageable pageable);

    boolean existsByTenantIdAndTypeKey(Long tenantId, String typeKey);

    @Modifying
    @Query("UPDATE IssueTypeModel i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.id = :id AND i.tenantId = :tenantId AND i.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
