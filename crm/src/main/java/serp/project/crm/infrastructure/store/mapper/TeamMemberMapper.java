/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.infrastructure.store.model.TeamMemberModel;

@Component
@RequiredArgsConstructor
public class TeamMemberMapper extends BaseMapper {

    public TeamMemberEntity toEntity(TeamMemberModel model) {
        if (model == null) {
            return null;
        }

        return TeamMemberEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .email(model.getEmail())
                .phone(model.getPhone())
                .teamId(model.getTeamId())
                .userId(model.getUserId())
                .role(model.getRole())
                .status(stringToEnum(model.getStatus(), TeamMemberStatus.class))
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public TeamMemberModel toModel(TeamMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        return TeamMemberModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .teamId(entity.getTeamId())
                .userId(entity.getUserId())
                .role(entity.getRole())
                .status(enumToString(entity.getStatus()))
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<TeamMemberEntity> toEntityList(List<TeamMemberModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<TeamMemberModel> toModelList(List<TeamMemberEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toModel).toList();
    }
}
