/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.NoteEntity;
import serp.project.crm.infrastructure.store.model.NoteModel;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NoteMapper extends BaseMapper {

    public NoteEntity toEntity(NoteModel model) {
        if (model == null) {
            return null;
          }
        return NoteEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .entityType(model.getEntityType())
                .entityId(model.getEntityId())
                .content(model.getContent())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public NoteModel toModel(NoteEntity entity) {
        if (entity == null) {
            return null;
        }
        return NoteModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .content(entity.getContent())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<NoteEntity> toEntityList(List<NoteModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
