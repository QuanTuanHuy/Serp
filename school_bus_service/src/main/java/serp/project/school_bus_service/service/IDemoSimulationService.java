package serp.project.school_bus_service.service;
import serp.project.school_bus_service.entity.DemoSessionEntity;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.request.DemoSpeedRequest;
import serp.project.school_bus_service.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.dto.response.DemoSessionResponse;

import java.util.List;

public interface IDemoSimulationService extends IBaseService<DemoSessionEntity, Long> {

    DemoSessionResponse startDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse pauseDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse resumeDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse stopDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse changeSpeed(Long tripId, DemoSpeedRequest request, Long tenantId, Long actorId);

    DemoSessionResponse getState(Long tripId, Long tenantId);

    List<DemoEventLogResponse> getEvents(Long tripId, Long tenantId);
}
