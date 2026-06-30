/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.NoteEntity;
import serp.project.crm.core.port.store.INotePort;
import serp.project.crm.infrastructure.store.mapper.NoteMapper;
import serp.project.crm.infrastructure.store.repository.NoteRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NoteAdapter implements INotePort {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    @Override
    public NoteEntity save(NoteEntity noteEntity) {
        var model = noteMapper.toModel(noteEntity);
        return noteMapper.toEntity(noteRepository.save(model));
    }

    @Override
    public Optional<NoteEntity> findById(Long id, Long tenantId) {
        return noteRepository.findByIdAndTenantId(id, tenantId)
                .map(noteMapper::toEntity);
    }

    @Override
    public Pair<List<NoteEntity>, Long> findByEntity(String entityType, Long entityId, Long tenantId,
            PageRequest pageRequest) {
        var pageable = noteMapper.toPageable(pageRequest);
        var page = noteRepository.findByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                tenantId, entityType, entityId, pageable)
                .map(noteMapper::toEntity);
        return noteMapper.pageToPair(page);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        noteRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(noteRepository::delete);
    }
}
