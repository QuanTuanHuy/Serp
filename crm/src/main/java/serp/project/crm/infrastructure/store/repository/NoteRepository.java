/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.NoteModel;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<NoteModel, Long> {
    Optional<NoteModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<NoteModel> findByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            Long tenantId, String entityType, Long entityId, Pageable pageable);
}
