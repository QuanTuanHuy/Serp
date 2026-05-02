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
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateMeetingRequest;
import serp.project.crm.core.domain.dto.response.MeetingRequestResponse;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.mapper.MeetingRequestDtoMapper;
import serp.project.crm.core.service.IMeetingRequestService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingRequestUseCase {

    private final IMeetingRequestService meetingRequestService;
    private final MeetingRequestDtoMapper meetingRequestDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createMeetingRequest(CreateMeetingRequest request, Long userId, Long tenantId) {
        MeetingRequestEntity entity = meetingRequestDtoMapper.toEntity(request);
        MeetingRequestEntity created = meetingRequestService.createMeetingRequest(entity, userId, tenantId);
        return responseUtils.success(meetingRequestDtoMapper.toResponse(created), "Meeting request created successfully");
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getMeetingRequestById(Long id, Long tenantId) {
        MeetingRequestEntity entity = meetingRequestService.getMeetingRequestById(id, tenantId).orElse(null);
        if (entity == null) {
            return responseUtils.notFound(ErrorMessage.MEETING_REQUEST_NOT_FOUND);
        }
        return responseUtils.success(meetingRequestDtoMapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getMeetingRequests(Long tenantId, PageRequest pageRequest, MeetingRequestStatus status) {
        var result = meetingRequestService.getMeetingRequests(tenantId, pageRequest, status);
        List<MeetingRequestResponse> responses = result.getFirst().stream()
                .map(meetingRequestDtoMapper::toResponse)
                .toList();
        return responseUtils.success(PageResponse.of(responses, pageRequest, result.getSecond()));
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelMeetingRequest(Long id, Long userId, Long tenantId) {
        MeetingRequestEntity cancelled = meetingRequestService.cancelMeetingRequest(id, userId, tenantId);
        return responseUtils.success(meetingRequestDtoMapper.toResponse(cancelled), "Meeting request cancelled successfully");
    }
}
