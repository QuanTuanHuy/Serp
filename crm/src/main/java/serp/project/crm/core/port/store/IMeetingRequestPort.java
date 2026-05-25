/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;

import java.util.List;
import java.util.Optional;

public interface IMeetingRequestPort {

    MeetingRequestEntity save(MeetingRequestEntity meetingRequestEntity);

    Optional<MeetingRequestEntity> findById(Long id, Long tenantId);

    Pair<List<MeetingRequestEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<MeetingRequestEntity>, Long> findByStatus(MeetingRequestStatus status, Long tenantId, PageRequest pageRequest);

    List<MeetingRequestEntity> findPendingRequests(long nowMs, int limit);
}
