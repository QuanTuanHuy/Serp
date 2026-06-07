/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateMeetingRequest;
import serp.project.crm.core.domain.dto.response.MeetingRequestResponse;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.mapper.MeetingRequestDtoMapper;
import serp.project.crm.core.service.IAccountService;
import serp.project.crm.core.service.IContactService;
import serp.project.crm.core.service.IMeetingRequestService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingRequestUseCase {

    private final IMeetingRequestService meetingRequestService;
    private final IAccountService accountService;
    private final IOpportunityService opportunityService;
    private final IContactService contactService;
    private final MeetingRequestDtoMapper meetingRequestDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createMeetingRequest(CreateMeetingRequest request, Long userId, Long tenantId) {
        MeetingRequestEntity entity = meetingRequestDtoMapper.toEntity(request);
        MeetingRequestEntity created = meetingRequestService.createMeetingRequest(entity, userId, tenantId);
        return responseUtils.success(meetingRequestDtoMapper.toResponse(created),
                "Meeting request created successfully");
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getMeetingRequestById(Long id, Long tenantId) {
        MeetingRequestEntity entity = meetingRequestService.getMeetingRequestById(id, tenantId).orElse(null);
        if (entity == null) {
            return responseUtils.notFound(ErrorMessage.MEETING_REQUEST_NOT_FOUND);
        }
        MeetingRequestResponse response = meetingRequestDtoMapper.toResponse(entity);
        if (entity.getAccountId() != null) {
            accountService.getAccountById(entity.getAccountId(), tenantId)
                    .ifPresent(account -> response.setAccountName(account.getName()));
        }
        if (entity.getOpportunityId() != null) {
            opportunityService.getOpportunityById(entity.getOpportunityId(), tenantId)
                    .ifPresent(opportunity -> response.setOpportunityName(opportunity.getName()));
        }
        if (entity.getContactId() != null) {
            contactService.getContactById(entity.getContactId(), tenantId)
                    .ifPresent(contact -> response.setContactName(contact.getName()));
        }
        return responseUtils.success(response);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getMeetingRequests(Long tenantId, PageRequest pageRequest, MeetingRequestStatus status) {
        var result = meetingRequestService.getMeetingRequests(tenantId, pageRequest, status);
        List<MeetingRequestEntity> entities = result.getFirst();
        if (CollectionUtils.isEmpty(entities)) {
            return responseUtils.success(PageResponse.of(Collections.emptyList(), pageRequest, 0));
        }

        List<Long> acocuntIds = entities.stream().map(MeetingRequestEntity::getAccountId)
                .filter(Objects::nonNull)
                .distinct().toList();
        List<AccountEntity> accounts = accountService.getAccountsByIds(acocuntIds, tenantId);
        Map<Long, String> accountNameMap = accounts.stream()
                .collect(Collectors.toMap(AccountEntity::getId, AccountEntity::getName));

        List<Long> opportunityIds = entities.stream().map(MeetingRequestEntity::getOpportunityId)
                .filter(Objects::nonNull)
                .distinct().toList();
        List<OpportunityEntity> opportunities = opportunityService.getOpportunitiesByIds(opportunityIds, tenantId);
        Map<Long, String> opportunityNameMap = opportunities.stream()
                .collect(Collectors.toMap(OpportunityEntity::getId, OpportunityEntity::getName));

        List<Long> contactIds = entities.stream().map(MeetingRequestEntity::getContactId)
                .filter(Objects::nonNull)
                .distinct().toList();
        List<ContactEntity> contacts = contactService.getContactsByIds(contactIds, tenantId);
        Map<Long, String> contactNameMap = contacts.stream()
                .collect(Collectors.toMap(ContactEntity::getId, ContactEntity::getName));

        List<MeetingRequestResponse> responses = entities.stream()
                .map(entity -> {
                    MeetingRequestResponse response = meetingRequestDtoMapper.toResponse(entity);
                    if (response.getAccountId() != null) {
                        response.setAccountName(
                                accountNameMap.getOrDefault(response.getAccountId(),
                                        "Account #" + response.getAccountId()));
                    }
                    if (response.getOpportunityId() != null) {
                        response.setOpportunityName(
                                opportunityNameMap.getOrDefault(response.getOpportunityId(),
                                        "Opportunity #" + response.getOpportunityId()));
                    }
                    if (response.getContactId() != null) {
                        response.setContactName(
                                contactNameMap.getOrDefault(response.getContactId(),
                                        "Contact #" + response.getContactId()));
                    }
                    return response;
                })
                .toList();
        return responseUtils.success(PageResponse.of(responses, pageRequest, result.getSecond()));
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelMeetingRequest(Long id, Long userId, Long tenantId) {
        MeetingRequestEntity cancelled = meetingRequestService.cancelMeetingRequest(id, userId, tenantId);
        return responseUtils.success(meetingRequestDtoMapper.toResponse(cancelled),
                "Meeting request cancelled successfully");
    }
}
