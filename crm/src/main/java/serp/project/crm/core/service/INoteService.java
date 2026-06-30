/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.NoteEntity;

import java.util.List;

public interface INoteService {
    NoteEntity createNote(NoteEntity note, Long tenantId);
    NoteEntity updateNote(Long id, String content, Long userId, Long tenantId);
    Pair<List<NoteEntity>, Long> getNotesByEntity(String entityType, Long entityId, Long tenantId, PageRequest pageRequest);
    void deleteNote(Long id, Long tenantId);
}
