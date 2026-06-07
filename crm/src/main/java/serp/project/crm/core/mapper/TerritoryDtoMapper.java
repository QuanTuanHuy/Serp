/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateTerritoryRequest;
import serp.project.crm.core.domain.dto.request.ResolveTerritoryRequest;
import serp.project.crm.core.domain.dto.request.UpdateTerritoryRequest;
import serp.project.crm.core.domain.dto.response.ResolveTerritoryResponse;
import serp.project.crm.core.domain.dto.response.TeamTerritoryResponse;
import serp.project.crm.core.domain.dto.response.TerritoryOwnerResponse;
import serp.project.crm.core.domain.dto.response.TerritoryResponse;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.domain.entity.TeamTerritoryEntity;
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
                .source(entity.isGlobal() ? "GLOBAL" : "TENANT")
                .build();
    }

    public TerritoryEntity toEntity(CreateTerritoryRequest request) {
        return TerritoryEntity.builder()
                .territoryCode(request.getTerritoryCode())
                .territoryName(request.getTerritoryName())
                .territoryLevel(request.getTerritoryLevel())
                .countryCode(request.getCountryCode())
                .parentTerritoryCode(request.getParentTerritoryCode())
                .active(request.getActive())
                .build();
    }

    public TerritoryEntity toEntity(UpdateTerritoryRequest request) {
        return TerritoryEntity.builder()
                .territoryName(request.getTerritoryName())
                .territoryLevel(request.getTerritoryLevel())
                .countryCode(request.getCountryCode())
                .parentTerritoryCode(request.getParentTerritoryCode())
                .active(request.getActive())
                .build();
    }

    public TeamTerritoryResponse toTeamTerritoryResponse(Long teamId, List<TerritoryEntity> territories) {
        return TeamTerritoryResponse.builder()
                .teamId(teamId)
                .territories(territories.stream().map(this::toResponse).toList())
                .build();
    }

    public ResolveTerritoryResponse toResolveResponse(TerritoryEntity entity, ResolveTerritoryRequest request) {
        if (entity == null) {
            return ResolveTerritoryResponse.builder().resolvedBy("UNRESOLVED").build();
        }

        return ResolveTerritoryResponse.builder()
                .territoryCode(entity.getTerritoryCode())
                .territoryName(entity.getTerritoryName())
                .resolvedBy(request.getTerritoryCode() != null && !request.getTerritoryCode().isBlank()
                        ? "DIRECT_CODE"
                        : "STATE_CITY_MATCH")
                .build();
    }

    public TerritoryOwnerResponse toOwnerResponse(String territoryCode, TeamTerritoryEntity mapping, TeamEntity team) {
        return TerritoryOwnerResponse.builder()
                .territoryCode(territoryCode)
                .teamId(team.getId())
                .teamName(team.getName())
                .active(mapping.getActive())
                .assignedAt(mapping.getCreatedAt())
                .assignedBy(mapping.getAssignedBy())
                .build();
    }
}
