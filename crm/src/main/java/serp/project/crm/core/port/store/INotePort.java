/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.NoteEntity;

import java.util.List;
import java.util.Optional;

public interface INotePort {
    NoteEntity save(NoteEntity note);
    Optional<NoteEntity> findById(Long id, Long tenantId);
    Pair<List<NoteEntity>, Long> findByEntity(String entityType, Long entityId, Long tenantId, PageRequest pageRequest);
    void delete(Long id, Long tenantId);
}
