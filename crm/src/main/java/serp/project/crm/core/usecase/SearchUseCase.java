/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.crm.core.usecase;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.AccountFilterRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.dto.response.AccountResponse;
import serp.project.crm.core.domain.dto.response.GlobalSearchResponse;
import serp.project.crm.core.domain.dto.response.GlobalSearchResponse.GlobalSearchSection;
import serp.project.crm.core.domain.dto.response.LeadResponse;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.mapper.AccountDtoMapper;
import serp.project.crm.core.mapper.LeadDtoMapper;
import serp.project.crm.core.mapper.OpportunityDtoMapper;
import serp.project.crm.core.service.IAccountService;
import serp.project.crm.core.service.ILeadService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchUseCase {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final ILeadService leadService;
    private final IOpportunityService opportunityService;
    private final IAccountService AccountService;

    private final LeadDtoMapper leadDtoMapper;
    private final OpportunityDtoMapper opportunityDtoMapper;
    private final AccountDtoMapper AccountDtoMapper;

    private final ResponseUtils responseUtils;

    @Transactional(readOnly = true)
    public GeneralResponse<?> globalSearch(String query, Long tenantId, Integer limit) {
        try {
            String keyword = query != null ? query.trim() : "";
            if (keyword.isEmpty()) {
                return responseUtils.badRequest("Query must not be empty");
            }

            int pageSize = normalizeLimit(limit);

            GlobalSearchSection<LeadResponse> leadSection = searchLeads(keyword, tenantId, pageSize);
            GlobalSearchSection<OpportunityResponse> opportunitySection = searchOpportunities(keyword, tenantId, pageSize);
            GlobalSearchSection<AccountResponse> accountSection = searchAccounts(keyword, tenantId, pageSize);

            GlobalSearchResponse response = GlobalSearchResponse.builder()
                    .leads(leadSection)
                    .opportunities(opportunitySection)
                    .accounts(accountSection)
                    .build();

            return responseUtils.success(response, "Global search executed successfully");
        } catch (Exception e) {
            log.error("Error executing global search: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to execute global search");
        }
    }

    private GlobalSearchSection<LeadResponse> searchLeads(String keyword, Long tenantId, int size) {
        LeadFilterRequest filter = LeadFilterRequest.builder()
                .keyword(keyword)
                .page(1)
                .size(size)
                .build();
        PageRequest pageRequest = filter.toPageRequest();
        Pair<List<LeadEntity>, Long> result = leadService.filterLeads(filter, tenantId, pageRequest);
        List<LeadResponse> items = result.getFirst().stream()
                .map(leadDtoMapper::toResponse)
                .toList();
        return GlobalSearchSection.<LeadResponse>builder()
                .items(items)
                .total(result.getSecond())
                .build();
    }

    private GlobalSearchSection<OpportunityResponse> searchOpportunities(String keyword, Long tenantId, int size) {
        OpportunityFilterRequest filter = OpportunityFilterRequest.builder()
                .keyword(keyword)
                .page(1)
                .size(size)
                .build();
        PageRequest pageRequest = filter.toPageRequest();
        Pair<List<OpportunityEntity>, Long> result = opportunityService.filterOpportunities(filter, tenantId, pageRequest);
        List<OpportunityResponse> items = result.getFirst().stream()
                .map(opportunityDtoMapper::toResponse)
                .toList();
        return GlobalSearchSection.<OpportunityResponse>builder()
                .items(items)
                .total(result.getSecond())
                .build();
    }

    private GlobalSearchSection<AccountResponse> searchAccounts(String keyword, Long tenantId, int size) {
        AccountFilterRequest filter = AccountFilterRequest.builder()
                .keyword(keyword)
                .page(1)
                .size(size)
                .build();
        PageRequest pageRequest = filter.toPageRequest();
        Pair<List<AccountEntity>, Long> result = AccountService.filterAccounts(filter, tenantId, pageRequest);
        List<AccountResponse> items = result.getFirst().stream()
                .map(AccountDtoMapper::toResponse)
                .toList();
        return GlobalSearchSection.<AccountResponse>builder()
                .items(items)
                .total(result.getSecond())
                .build();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
