/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateNoteRequest;
import serp.project.crm.core.domain.dto.response.NoteResponse;
import serp.project.crm.core.domain.entity.NoteEntity;

@Component
public class NoteDtoMapper {

    public NoteEntity toEntity(CreateNoteRequest request) {
        if (request == null) {
            return null;
        }
        return NoteEntity.builder()
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .content(request.getContent())
                .build();
    }

    public NoteResponse toResponse(NoteEntity entity) {
        if (entity == null) {
            return null;
        }
        return NoteResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
