/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.domain.enums.TeamStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.port.store.IMeetingRequestPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.ITeamPort;
import serp.project.crm.core.service.IActivityService;
import serp.project.crm.core.service.IMeetingRequestService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeetingRequestService implements IMeetingRequestService {

    private final IMeetingRequestPort meetingRequestPort;
    private final IAccountPort accountPort;
    private final IOpportunityPort opportunityPort;
    private final IContactPort contactPort;
    private final ITeamPort teamPort;
    private final IActivityService activityService;
    private final MeetingPriorityCalculator meetingPriorityCalculator;

    @Override
    @Transactional
    public MeetingRequestEntity createMeetingRequest(MeetingRequestEntity meetingRequest, Long userId, Long tenantId) {
        validateTimeWindow(meetingRequest);
        validateRelations(meetingRequest, tenantId);

        meetingRequest.setTenantId(tenantId);
        meetingRequest.setCreatedBy(userId);
        meetingRequest.setUpdatedBy(userId);
        meetingRequest.setDefaults();

        OpportunityEntity opportunity = meetingRequest.getOpportunityId() != null
                ? opportunityPort.findById(meetingRequest.getOpportunityId(), tenantId).orElse(null)
                : null;
        var account = accountPort.findById(meetingRequest.getAccountId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));

        meetingRequest.setPriorityScore(meetingPriorityCalculator.calculate(meetingRequest, account, opportunity));

        return meetingRequestPort.save(meetingRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MeetingRequestEntity> getMeetingRequestById(Long id, Long tenantId) {
        return meetingRequestPort.findById(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<MeetingRequestEntity>, Long> getMeetingRequests(Long tenantId, PageRequest pageRequest,
            MeetingRequestStatus status) {
        pageRequest.validate();
        if (status != null) {
            return meetingRequestPort.findByStatus(status, tenantId, pageRequest);
        }
        return meetingRequestPort.findAll(tenantId, pageRequest);
    }

    @Override
    @Transactional
    public MeetingRequestEntity cancelMeetingRequest(Long id, Long userId, Long tenantId) {
        MeetingRequestEntity existing = meetingRequestPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.MEETING_REQUEST_NOT_FOUND));

        if (MeetingRequestStatus.CANCELLED.equals(existing.getStatus())) {
            return existing;
        }
        if (MeetingRequestStatus.FAILED.equals(existing.getStatus())) {
            throw new AppException(ErrorMessage.MEETING_REQUEST_ALREADY_FINALIZED);
        }

        if (existing.getScheduledActivityId() != null) {
            activityService.cancelActivity(existing.getScheduledActivityId(), userId, tenantId);
        }

        existing.setStatus(MeetingRequestStatus.CANCELLED);
        existing.setFailureReason(null);
        existing.setUpdatedBy(userId);
        return meetingRequestPort.save(existing);
    }

    private void validateTimeWindow(MeetingRequestEntity meetingRequest) {
        if (meetingRequest.getEarliestStart() == null || meetingRequest.getLatestStart() == null
                || meetingRequest.getRequestedDeadline() == null || meetingRequest.getMeetingType() == null) {
            throw new AppException(ErrorMessage.MEETING_REQUEST_INVALID_WINDOW);
        }

        if (meetingRequest.getEarliestStart() > meetingRequest.getLatestStart()) {
            throw new AppException(ErrorMessage.MEETING_REQUEST_INVALID_WINDOW);
        }
        if (meetingRequest.getRequestedDeadline() < meetingRequest.getEarliestStart()) {
            throw new AppException(ErrorMessage.MEETING_REQUEST_INVALID_WINDOW);
        }
        if (!StringUtils.hasText(meetingRequest.getSubject()) && meetingRequest.getMeetingType() == null) {
            throw new AppException(ErrorMessage.MEETING_REQUEST_SUBJECT_REQUIRED);
        }
    }

    private void validateRelations(MeetingRequestEntity meetingRequest, Long tenantId) {
        var account = accountPort.findById(meetingRequest.getAccountId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        if (!account.isActive()) {
            throw new AppException(ErrorMessage.ACCOUNT_INACTIVE);
        }

        if (meetingRequest.getOpportunityId() != null
                && opportunityPort.findById(meetingRequest.getOpportunityId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND);
        }
        if (meetingRequest.getContactId() != null
                && contactPort.findById(meetingRequest.getContactId(), tenantId).isEmpty()) {
            throw new AppException(ErrorMessage.CONTACT_NOT_FOUND);
        }

        var team = teamPort.findById(meetingRequest.getTeamId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_NOT_FOUND));
        if (!TeamStatus.ACTIVE.equals(team.getStatus())) {
            throw new AppException(ErrorMessage.TEAM_INACTIVE);
        }
    }
}
