/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.TeamTerritoryEntity;
import serp.project.crm.infrastructure.store.model.TeamTerritoryModel;

@Component
public class TeamTerritoryMapper extends BaseMapper {
    public TeamTerritoryEntity toEntity(TeamTerritoryModel model) {
        if (model == null) {
            return null;
        }

        return TeamTerritoryEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .teamId(model.getTeamId())
                .territoryCode(model.getTerritoryCode())
                .assignedBy(model.getAssignedBy())
                .active(model.getActive())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public TeamTerritoryModel toModel(TeamTerritoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return TeamTerritoryModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .teamId(entity.getTeamId())
                .territoryCode(entity.getTerritoryCode())
                .assignedBy(entity.getAssignedBy())
                .active(entity.getActive())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
