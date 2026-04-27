/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.response.TeamTerritoryResponse;
import serp.project.crm.core.domain.dto.response.TerritoryResponse;
import serp.project.crm.core.domain.entity.TerritoryEntity;

import java.util.List;

@Component
public class TerritoryDtoMapper {
    public TerritoryResponse toResponse(TerritoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return TerritoryResponse.builder()
                .id(entity.getId())
                .territoryCode(entity.getTerritoryCode())
                .territoryName(entity.getTerritoryName())
                .territoryLevel(entity.getTerritoryLevel())
                .countryCode(entity.getCountryCode())
                .parentTerritoryCode(entity.getParentTerritoryCode())
                .active(entity.getActive())
                .build();
    }

    public TeamTerritoryResponse toTeamTerritoryResponse(Long teamId, List<TerritoryEntity> territories) {
        return TeamTerritoryResponse.builder()
                .teamId(teamId)
                .territories(territories.stream().map(this::toResponse).toList())
                .build();
    }
}
