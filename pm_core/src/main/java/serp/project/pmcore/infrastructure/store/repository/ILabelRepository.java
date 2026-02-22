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
import serp.project.pmcore.infrastructure.store.model.LabelModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILabelRepository extends JpaRepository<LabelModel, Long> {

    Optional<LabelModel> findByIdAndTenantId(Long id, Long tenantId);

    List<LabelModel> findAllByTenantIdAndProjectId(Long tenantId, Long projectId);

    @Modifying
    @Query("UPDATE LabelModel l SET l.deletedAt = CURRENT_TIMESTAMP WHERE l.id = :id AND l.tenantId = :tenantId AND l.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
