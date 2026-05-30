package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.entity.DemoEventLogEntity;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.enums.DemoEventType;

import java.util.List;

public interface IDemoEventLogService {

    DemoEventLogEntity record(DemoSessionEntity session, DemoEventType eventType, String payloadJson,
                Long tenantId, Long actorId);

    List<DemoEventLogResponse> getEvents(Long sessionId, Long tenantId);

    /** Internal: returns event entities for mapper composition. */
    List<DemoEventLogEntity> getEventEntities(Long sessionId, Long tenantId);
}
