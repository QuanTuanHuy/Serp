/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.TerritoryEntity;
import serp.project.crm.core.domain.enums.TerritoryLevel;
import serp.project.crm.infrastructure.store.model.TerritoryModel;

@Component
public class TerritoryMapper extends BaseMapper {
    public TerritoryEntity toEntity(TerritoryModel model) {
        if (model == null) {
            return null;
        }

        return TerritoryEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .territoryCode(model.getTerritoryCode())
                .territoryName(model.getTerritoryName())
                .territoryLevel(stringToEnum(model.getTerritoryLevel(), TerritoryLevel.class))
                .countryCode(model.getCountryCode())
                .parentTerritoryCode(model.getParentTerritoryCode())
                .active(model.getActive())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public TerritoryModel toModel(TerritoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return TerritoryModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .territoryCode(entity.getTerritoryCode())
                .territoryName(entity.getTerritoryName())
                .territoryLevel(enumToString(entity.getTerritoryLevel()))
                .countryCode(entity.getCountryCode())
                .parentTerritoryCode(entity.getParentTerritoryCode())
                .active(entity.getActive())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
