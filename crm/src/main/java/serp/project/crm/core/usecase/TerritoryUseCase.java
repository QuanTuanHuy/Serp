/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.request.CreateTerritoryRequest;
import serp.project.crm.core.domain.dto.request.ManualRerouteLeadRequest;
import serp.project.crm.core.domain.dto.request.ResolveTerritoryRequest;
import serp.project.crm.core.domain.dto.request.TerritoryFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateTerritoryRequest;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.TerritoryDtoMapper;
import serp.project.crm.core.service.ILeadService;
import serp.project.crm.core.service.ITeamRoutingService;
import serp.project.crm.core.service.ITeamTerritoryService;
import serp.project.crm.core.service.ITerritoryService;
import serp.project.crm.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TerritoryUseCase {

    private final ITerritoryService territoryService;
    private final ITeamTerritoryService teamTerritoryService;
    private final ILeadService leadService;
    private final ITeamRoutingService teamRoutingService;
    private final TerritoryDtoMapper territoryDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createTerritory(CreateTerritoryRequest request, Long tenantId, Long userId) {
        var created = territoryService.createTerritory(territoryDtoMapper.toEntity(request), tenantId, userId);
        return responseUtils.success(territoryDtoMapper.toResponse(created), "Territory created successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateTerritory(String territoryCode, UpdateTerritoryRequest request, Long tenantId,
            Long userId) {
        var updated = territoryService.updateTerritory(territoryCode, territoryDtoMapper.toEntity(request), tenantId,
                userId);
        return responseUtils.success(territoryDtoMapper.toResponse(updated), "Territory updated successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> activateTerritory(String territoryCode, Long tenantId, Long userId) {
        var updated = territoryService.activateTerritory(territoryCode, tenantId, userId);
        return responseUtils.success(territoryDtoMapper.toResponse(updated), "Territory activated successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deactivateTerritory(String territoryCode, Long tenantId, Long userId) {
        var updated = territoryService.deactivateTerritory(territoryCode, tenantId, userId);
        return responseUtils.success(territoryDtoMapper.toResponse(updated), "Territory deactivated successfully");
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTerritoryByCode(String territoryCode, Long tenantId) {
        return territoryService.getTerritoryByCode(territoryCode, tenantId)
                .<GeneralResponse<?>>map(entity -> responseUtils.success(territoryDtoMapper.toResponse(entity)))
                .orElseGet(() -> responseUtils.notFound(ErrorMessage.TERRITORY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTerritories(TerritoryFilterRequest filter, Long tenantId) {
        var territories = territoryService.getTerritories(filter, tenantId).stream()
                .map(territoryDtoMapper::toResponse)
                .toList();
        return responseUtils.success(territories);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTerritoryOwner(String territoryCode, Long tenantId) {
        return teamTerritoryService.getTerritoryOwner(territoryCode, tenantId)
                .<GeneralResponse<?>>map(responseUtils::success)
                .orElseGet(() -> responseUtils.notFound(ErrorMessage.TERRITORY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> resolveTerritory(ResolveTerritoryRequest request, Long tenantId) {
        var resolved = territoryService.resolveTerritory(request.getTerritoryCode(), request.getState(), request.getCity(),
                tenantId).orElse(null);
        return responseUtils.success(territoryDtoMapper.toResolveResponse(resolved, request));
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> rerouteLead(Long leadId, ManualRerouteLeadRequest request, Long tenantId, Long userId) {
        var lead = leadService.getLeadById(leadId, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.LEAD_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getForceReroute())) {
            lead.setAssignedTo(null);
        }

        if (lead.getAssignedTo() == null) {
            teamRoutingService.routeLeadAssignee(lead.getTerritoryCode(), tenantId)
                    .ifPresentOrElse(lead::setAssignedTo, () -> lead.setAssignedTo(null));
        }

        lead.setUpdatedBy(userId);
        var updated = leadService.updateLead(leadId, lead, tenantId);
        return responseUtils.success(updated, "Lead rerouted successfully");
    }
}
