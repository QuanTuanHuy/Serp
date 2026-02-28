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

import java.util.Optional;

@Repository
public interface IProjectRepository extends JpaRepository<ProjectModel, Long> {

    Optional<ProjectModel> findByIdAndTenantId(Long id, Long tenantId);

    Optional<ProjectModel> findByKeyAndTenantId(String key, Long tenantId);

    boolean existsByKeyAndTenantId(String key, Long tenantId);

    Page<ProjectModel> findAllByTenantId(Long tenantId, Pageable pageable);

    @Query("SELECT p FROM ProjectModel p WHERE p.tenantId = :tenantId " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.key) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR p.projectCategoryId = :categoryId) " +
            "AND (:projectTypeKey IS NULL OR p.projectTypeKey = :projectTypeKey) " +
            "AND (:archived IS NULL OR p.archived = :archived)")
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
