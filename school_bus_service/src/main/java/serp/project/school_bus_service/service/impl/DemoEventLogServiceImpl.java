package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.entity.DemoEventLogEntity;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.enums.DemoEventType;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.DemoEventLogRepository;
import serp.project.school_bus_service.service.IDemoEventLogService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DemoEventLogServiceImpl implements IDemoEventLogService {

    private final DemoEventLogRepository demoEventLogRepository;
    private final SchoolBusMapper mapper;

    public DemoEventLogServiceImpl(DemoEventLogRepository demoEventLogRepository,
                                   SchoolBusMapper mapper) {
        this.demoEventLogRepository = demoEventLogRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void record(DemoSessionEntity session, DemoEventType eventType, String payloadJson,
                       Long tenantId, Long actorId) {
        DemoEventLogEntity event = new DemoEventLogEntity();
        event.markCreated(tenantId, actorId.toString());
        event.setDemoSession(session);
        event.setEventType(eventType);
        event.setEventTime(LocalDateTime.now());
        event.setPayloadJson(payloadJson);
        demoEventLogRepository.save(event);
    }

    @Override
    public List<DemoEventLogResponse> getEvents(Long sessionId, Long tenantId) {
        return demoEventLogRepository
                .findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeDesc(sessionId, tenantId)
                .stream()
                .map(mapper::toDemoEventLogResponse)
                .toList();
    }

    @Override
    public List<DemoEventLogEntity> getEventEntities(Long sessionId, Long tenantId) {
        return demoEventLogRepository
                .findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeDesc(sessionId, tenantId);
    }
}
