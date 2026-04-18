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
import serp.project.pmcore.infrastructure.store.model.ProjectModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProjectRepository extends JpaRepository<ProjectModel, Long> {

    Optional<ProjectModel> findByIdAndTenantId(Long id, Long tenantId);

    Optional<ProjectModel> findByKeyAndTenantId(String key, Long tenantId);

    boolean existsByKeyAndTenantId(String key, Long tenantId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProjectModel p " +
            "WHERE p.projectCategoryId = :categoryId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    boolean existsActiveProjectByCategoryId(@Param("categoryId") Long categoryId, @Param("tenantId") Long tenantId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProjectModel p " +
            "WHERE p.issueTypeSchemeId = :issueTypeSchemeId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    boolean existsActiveProjectByIssueTypeSchemeId(@Param("issueTypeSchemeId") Long issueTypeSchemeId,
                                                   @Param("tenantId") Long tenantId);

    @Query("SELECT p.id FROM ProjectModel p WHERE p.issueTypeSchemeId = :issueTypeSchemeId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    List<Long> findActiveProjectIdsByIssueTypeSchemeId(@Param("issueTypeSchemeId") Long issueTypeSchemeId,
                                                       @Param("tenantId") Long tenantId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProjectModel p " +
            "WHERE p.prioritySchemeId = :prioritySchemeId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    boolean existsActiveProjectByPrioritySchemeId(@Param("prioritySchemeId") Long prioritySchemeId,
                                                  @Param("tenantId") Long tenantId);

    @Query("SELECT p.id FROM ProjectModel p WHERE p.prioritySchemeId = :prioritySchemeId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    List<Long> findActiveProjectIdsByPrioritySchemeId(@Param("prioritySchemeId") Long prioritySchemeId,
                                                      @Param("tenantId") Long tenantId);

    Page<ProjectModel> findAllByTenantId(Long tenantId, Pageable pageable);

    @Query(value = """
    SELECT *
    FROM projects p
    WHERE p.tenant_id = :tenantId
      AND p.deleted_at IS NULL
      AND (
            :search IS NULL
            OR p.name ILIKE CONCAT('%', :search, '%')
            OR p.key ILIKE CONCAT('%', :search, '%')
          )
      AND (:categoryId IS NULL OR p.project_category_id = :categoryId)
      AND (:projectTypeKey IS NULL OR p.project_type_key = :projectTypeKey)
      AND (:archived IS NULL OR p.archived = :archived)
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM projects p
    WHERE p.tenant_id = :tenantId
      AND p.deleted_at IS NULL
      AND (
            :search IS NULL
            OR p.name ILIKE CONCAT('%', :search, '%')
            OR p.key ILIKE CONCAT('%', :search, '%')
          )
      AND (:categoryId IS NULL OR p.project_category_id = :categoryId)
      AND (:projectTypeKey IS NULL OR p.project_type_key = :projectTypeKey)
      AND (:archived IS NULL OR p.archived = :archived)
    """,
            nativeQuery = true)
    Page<ProjectModel> findAllWithFilters(
            @Param("tenantId") Long tenantId,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("projectTypeKey") String projectTypeKey,
            @Param("archived") Boolean archived,
            Pageable pageable);

    @Modifying
    @Query("UPDATE ProjectModel p SET p.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE p.id = :id AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    void softDeleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
