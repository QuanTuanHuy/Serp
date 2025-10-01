/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.infrastructure.store.model.TeamModel;

@Component
@RequiredArgsConstructor
public class TeamMapper extends BaseMapper {

    public TeamEntity toEntity(TeamModel model) {
        if (model == null) {
            return null;
        }

        return TeamEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .leaderId(model.getLeaderId())
                .notes(model.getNotes())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public TeamModel toModel(TeamEntity entity) {
        if (entity == null) {
            return null;
        }

        return TeamModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .leaderId(entity.getLeaderId())
                .notes(entity.getNotes())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<TeamEntity> toEntityList(List<TeamModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }

}
