/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.TeamMemberModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMemberModel, Long> {

    Optional<TeamMemberModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<TeamMemberModel> findByTenantId(Long tenantId, Pageable pageable);

    Page<TeamMemberModel> findByTenantIdAndTeamId(Long tenantId, Long teamId, Pageable pageable);

    Optional<TeamMemberModel> findByTenantIdAndUserId(Long tenantId, Long userId);

    List<TeamMemberModel> findByTenantIdAndTeamIdAndStatus(Long tenantId, Long teamId, String status);

    Optional<TeamMemberModel> findByTenantIdAndTeamIdAndUserId(Long tenantId, Long teamId, Long userId);

    @Query("SELECT tm FROM TeamMemberModel tm WHERE tm.tenantId = :tenantId " +
            "AND (LOWER(tm.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(tm.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TeamMemberModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    boolean existsByTenantIdAndTeamIdAndUserId(Long tenantId, Long teamId, Long userId);

    long countByTenantIdAndTeamId(Long tenantId, Long teamId);

    long countByTenantIdAndTeamIdAndStatus(Long tenantId, Long teamId, String status);

    long countByTenantIdAndStatus(Long tenantId, String status);

    void deleteByTeamIdAndTenantId(Long teamId, Long tenantId);

}
