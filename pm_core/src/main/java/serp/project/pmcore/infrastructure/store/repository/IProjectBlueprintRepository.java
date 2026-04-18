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
import serp.project.pmcore.infrastructure.store.model.ProjectBlueprintModel;

import java.util.Optional;

@Repository
public interface IProjectBlueprintRepository extends JpaRepository<ProjectBlueprintModel, Long> {

    Optional<ProjectBlueprintModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT b FROM ProjectBlueprintModel b WHERE b.id = :id AND (b.tenantId = :tenantId OR b.tenantId = 0)")
    Optional<ProjectBlueprintModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query(value = """
    SELECT *
    FROM project_blueprints b
    WHERE (b.tenant_id = :tenantId OR b.tenant_id = 0)
      AND b.deleted_at IS NULL
      AND (:search IS NULL OR b.name ILIKE CONCAT('%', :search, '%'))
      AND (:projectTypeKey IS NULL OR b.project_type_key = :projectTypeKey)
      AND (:isSystem IS NULL OR b.is_system = :isSystem)
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM project_blueprints b
    WHERE (b.tenant_id = :tenantId OR b.tenant_id = 0)
      AND b.deleted_at IS NULL
      AND (:search IS NULL OR b.name ILIKE CONCAT('%', :search, '%'))
      AND (:projectTypeKey IS NULL OR b.project_type_key = :projectTypeKey)
      AND (:isSystem IS NULL OR b.is_system = :isSystem)
    """,
            nativeQuery = true)
    Page<ProjectBlueprintModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                          @Param("search") String search,
                                                          @Param("projectTypeKey") String projectTypeKey,
                                                          @Param("isSystem") Boolean isSystem,
                                                          Pageable pageable);

    boolean existsByTenantIdAndName(Long tenantId, String name);
}
