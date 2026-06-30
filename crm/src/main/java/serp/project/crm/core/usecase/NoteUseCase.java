/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateNoteRequest;
import serp.project.crm.core.domain.dto.response.NoteResponse;
import serp.project.crm.core.domain.entity.NoteEntity;
import serp.project.crm.core.mapper.NoteDtoMapper;
import serp.project.crm.core.service.INoteService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteUseCase {

    private final INoteService noteService;
    private final NoteDtoMapper noteDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createNote(CreateNoteRequest request, Long userId, Long tenantId) {
        NoteEntity noteEntity = noteDtoMapper.toEntity(request);
        noteEntity.setCreatedBy(userId);
        NoteEntity saved = noteService.createNote(noteEntity, tenantId);
        return responseUtils.success(noteDtoMapper.toResponse(saved), "Note created successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateNote(Long id, String content, Long userId, Long tenantId) {
        NoteEntity updated = noteService.updateNote(id, content, userId, tenantId);
        return responseUtils.success(noteDtoMapper.toResponse(updated), "Note updated successfully");
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getNotesByEntity(String entityType, Long entityId, Long tenantId, PageRequest pageRequest) {
        var result = noteService.getNotesByEntity(entityType, entityId, tenantId, pageRequest);
        List<NoteResponse> responses = result.getFirst().stream()
                .map(noteDtoMapper::toResponse)
                .toList();
        return responseUtils.success(PageResponse.of(responses, pageRequest, result.getSecond()));
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteNote(Long id, Long tenantId) {
        noteService.deleteNote(id, tenantId);
        return responseUtils.status("Note deleted successfully");
    }
}
