/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.MeetingRequestEntity;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.port.store.IMeetingRequestPort;
import serp.project.crm.infrastructure.store.mapper.MeetingRequestMapper;
import serp.project.crm.infrastructure.store.repository.MeetingRequestRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MeetingRequestAdapter implements IMeetingRequestPort {

    private final MeetingRequestRepository meetingRequestRepository;
    private final MeetingRequestMapper meetingRequestMapper;

    @Override
    public MeetingRequestEntity save(MeetingRequestEntity meetingRequestEntity) {
        var model = meetingRequestMapper.toModel(meetingRequestEntity);
        return meetingRequestMapper.toEntity(meetingRequestRepository.save(model));
    }

    @Override
    public Optional<MeetingRequestEntity> findById(Long id, Long tenantId) {
        return meetingRequestRepository.findByIdAndTenantId(id, tenantId)
                .map(meetingRequestMapper::toEntity);
    }

    @Override
    public Pair<List<MeetingRequestEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var pageable = meetingRequestMapper.toPageable(pageRequest);
        var page = meetingRequestRepository.findByTenantId(tenantId, pageable)
                .map(meetingRequestMapper::toEntity);
        return meetingRequestMapper.pageToPair(page);
    }

    @Override
    public Pair<List<MeetingRequestEntity>, Long> findByStatus(MeetingRequestStatus status, Long tenantId,
            PageRequest pageRequest) {
        var pageable = meetingRequestMapper.toPageable(pageRequest);
        var page = meetingRequestRepository.findByTenantIdAndStatus(tenantId, status.name(), pageable)
                .map(meetingRequestMapper::toEntity);
        return meetingRequestMapper.pageToPair(page);
    }

    @Override
    public List<MeetingRequestEntity> findPendingRequests(long nowMs, int limit) {
        return meetingRequestRepository
                .findTop100ByStatusAndLatestStartGreaterThanOrderByRequestedDeadlineAscPriorityScoreDescCreatedAtAsc(
                        MeetingRequestStatus.PENDING.name(),
                        nowMs)
                .stream()
                .limit(limit)
                .map(meetingRequestMapper::toEntity)
                .toList();
    }
}
