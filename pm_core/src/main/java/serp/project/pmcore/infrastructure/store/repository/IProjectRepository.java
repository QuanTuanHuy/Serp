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

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProjectModel p " +
            "WHERE p.workflowSchemeId = :workflowSchemeId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    boolean existsActiveProjectByWorkflowSchemeId(@Param("workflowSchemeId") Long workflowSchemeId,
                                                  @Param("tenantId") Long tenantId);

    @Query("SELECT p.id FROM ProjectModel p WHERE p.workflowSchemeId = :workflowSchemeId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    List<Long> findActiveProjectIdsByWorkflowSchemeId(@Param("workflowSchemeId") Long workflowSchemeId,
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
      AND EXISTS (
            SELECT 1
            FROM permission_scheme_entries pse
            WHERE pse.scheme_id = p.permission_scheme_id
              AND pse.tenant_id = p.tenant_id
              AND pse.deleted_at IS NULL
              AND UPPER(TRIM(pse.permission_key)) = 'BROWSE_PROJECTS'
              AND (
                    (UPPER(TRIM(pse.grantee_type)) = 'USER' AND CAST(:userId AS TEXT) = pse.grantee_ref)
                    OR (
                        UPPER(TRIM(pse.grantee_type)) = 'PROJECT_LEAD'
                        AND p.lead_user_id = :userId
                    )
                    OR (
                        UPPER(TRIM(pse.grantee_type)) = 'GROUP'
                        AND :groupKeysCsv <> ''
                        AND POSITION(CONCAT(',', LOWER(TRIM(pse.grantee_ref)), ',') IN :groupKeysCsv) > 0
                    )
                    OR (
                        UPPER(TRIM(pse.grantee_type)) IN ('ANY_LOGGED_IN_USER', 'LOGGED_IN_USER', 'AUTHENTICATED')
                        AND :userId IS NOT NULL
                    )
                    OR (
                        UPPER(TRIM(pse.grantee_type)) = 'PROJECT_ROLE'
                        AND EXISTS (
                            SELECT 1
                            FROM project_roles pr
                            JOIN project_role_actors pra ON pra.project_role_id = pr.id
                            WHERE pra.project_id = p.id
                              AND pra.tenant_id = p.tenant_id
                              AND pra.deleted_at IS NULL
                              AND pr.deleted_at IS NULL
                              AND (pr.tenant_id = p.tenant_id OR pr.tenant_id = 0)
                              AND pr.name = pse.grantee_ref
                              AND (
                                    (UPPER(TRIM(pra.subject_type)) = 'USER' AND CAST(:userId AS TEXT) = pra.subject_id)
                                    OR (
                                        UPPER(TRIM(pra.subject_type)) = 'GROUP'
                                        AND :groupKeysCsv <> ''
                                        AND POSITION(CONCAT(',', LOWER(TRIM(pra.subject_id)), ',') IN :groupKeysCsv) > 0
                                    )
                              )
                        )
                    )
              )
      )
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
      AND EXISTS (
            SELECT 1
            FROM permission_scheme_entries pse
            WHERE pse.scheme_id = p.permission_scheme_id
              AND pse.tenant_id = p.tenant_id
              AND pse.deleted_at IS NULL
              AND UPPER(TRIM(pse.permission_key)) = 'BROWSE_PROJECTS'
              AND (
                    (UPPER(TRIM(pse.grantee_type)) = 'USER' AND CAST(:userId AS TEXT) = pse.grantee_ref)
                    OR (
                        UPPER(TRIM(pse.grantee_type)) = 'PROJECT_LEAD'
                        AND p.lead_user_id = :userId
                    )
                    OR (
                        UPPER(TRIM(pse.grantee_type)) = 'GROUP'
                        AND :groupKeysCsv <> ''
                        AND POSITION(CONCAT(',', LOWER(TRIM(pse.grantee_ref)), ',') IN :groupKeysCsv) > 0
                    )
                    OR (
                        UPPER(TRIM(pse.grantee_type)) IN ('ANY_LOGGED_IN_USER', 'LOGGED_IN_USER', 'AUTHENTICATED')
                        AND :userId IS NOT NULL
                    )
                    OR (
                        UPPER(TRIM(pse.grantee_type)) = 'PROJECT_ROLE'
                        AND EXISTS (
                            SELECT 1
                            FROM project_roles pr
                            JOIN project_role_actors pra ON pra.project_role_id = pr.id
                            WHERE pra.project_id = p.id
                              AND pra.tenant_id = p.tenant_id
                              AND pra.deleted_at IS NULL
                              AND pr.deleted_at IS NULL
                              AND (pr.tenant_id = p.tenant_id OR pr.tenant_id = 0)
                              AND pr.name = pse.grantee_ref
                              AND (
                                    (UPPER(TRIM(pra.subject_type)) = 'USER' AND CAST(:userId AS TEXT) = pra.subject_id)
                                    OR (
                                        UPPER(TRIM(pra.subject_type)) = 'GROUP'
                                        AND :groupKeysCsv <> ''
                                        AND POSITION(CONCAT(',', LOWER(TRIM(pra.subject_id)), ',') IN :groupKeysCsv) > 0
                                    )
                              )
                        )
                    )
              )
      )
    """,
            nativeQuery = true)
    Page<ProjectModel> findVisibleProjectsWithFilters(
            @Param("tenantId") Long tenantId,
            @Param("userId") Long userId,
            @Param("groupKeysCsv") String groupKeysCsv,
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
