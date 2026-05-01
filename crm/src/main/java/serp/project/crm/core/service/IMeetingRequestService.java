/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;

import java.util.List;
import java.util.Optional;

public interface IMeetingRequestService {

    MeetingRequestEntity createMeetingRequest(MeetingRequestEntity meetingRequest, Long userId, Long tenantId);

    Optional<MeetingRequestEntity> getMeetingRequestById(Long id, Long tenantId);

    Pair<List<MeetingRequestEntity>, Long> getMeetingRequests(Long tenantId, PageRequest pageRequest,
            MeetingRequestStatus status);

    MeetingRequestEntity cancelMeetingRequest(Long id, Long userId, Long tenantId);
}
