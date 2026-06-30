/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.NoteEntity;
import serp.project.crm.core.port.store.INotePort;
import serp.project.crm.core.service.INoteService;
import serp.project.crm.core.exception.AppException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService implements INoteService {

    private final INotePort notePort;

    @Override
    @Transactional
    public NoteEntity createNote(NoteEntity note, Long tenantId) {
        note.setTenantId(tenantId);
        return notePort.save(note);
    }

    @Override
    @Transactional
    public NoteEntity updateNote(Long id, String content, Long userId, Long tenantId) {
        NoteEntity existing = notePort.findById(id, tenantId)
                .orElseThrow(() -> new AppException("Note not found"));
        existing.setContent(content);
        existing.setUpdatedBy(userId);
        return notePort.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<NoteEntity>, Long> getNotesByEntity(String entityType, Long entityId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return notePort.findByEntity(entityType, entityId, tenantId, pageRequest);
    }

    @Override
    @Transactional
    public void deleteNote(Long id, Long tenantId) {
        notePort.delete(id, tenantId);
    }
}
