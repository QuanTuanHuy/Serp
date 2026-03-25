/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ProjectRoleActorModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProjectRoleActorRepository extends JpaRepository<ProjectRoleActorModel, Long> {
    Optional<ProjectRoleActorModel> findByTenantIdAndProjectIdAndProjectRoleIdAndSubjectTypeAndSubjectId(
            Long tenantId,
            Long projectId,
            Long projectRoleId,
            String subjectType,
            String subjectId
    );

    boolean existsByTenantIdAndProjectIdAndProjectRoleIdAndSubjectTypeAndSubjectId(
            Long tenantId,
            Long projectId,
            Long projectRoleId,
            String subjectType,
            String subjectId
    );

    List<ProjectRoleActorModel> findAllByProjectIdAndProjectRoleIdAndTenantId(Long projectId,
                                                                               Long projectRoleId,
                                                                               Long tenantId);

    @Modifying
    @Query("""
            UPDATE ProjectRoleActorModel a
            SET a.deletedAt = CURRENT_TIMESTAMP,
                a.updatedAt = CURRENT_TIMESTAMP,
                a.updatedBy = :updatedBy
            WHERE a.tenantId = :tenantId
              AND a.projectId = :projectId
              AND a.projectRoleId = :projectRoleId
              AND a.subjectType = :subjectType
              AND a.subjectId = :subjectId
              AND a.deletedAt IS NULL
            """)
    int softDeleteActiveAssignment(@Param("tenantId") Long tenantId,
                                   @Param("projectId") Long projectId,
                                   @Param("projectRoleId") Long projectRoleId,
                                   @Param("subjectType") String subjectType,
                                   @Param("subjectId") String subjectId,
                                   @Param("updatedBy") Long updatedBy);
}
