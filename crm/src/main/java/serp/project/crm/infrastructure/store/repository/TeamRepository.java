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
import serp.project.crm.infrastructure.store.model.TeamModel;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamModel, Long> {

    Optional<TeamModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<TeamModel> findByTenantId(Long tenantId, Pageable pageable);

    Page<TeamModel> findByTenantIdAndLeaderId(Long tenantId, Long leaderId, Pageable pageable);

    @Query("SELECT t FROM TeamModel t WHERE t.tenantId = :tenantId " +
            "AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TeamModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    boolean existsByNameAndTenantId(String name, Long tenantId);

    long countByTenantId(Long tenantId);
}
