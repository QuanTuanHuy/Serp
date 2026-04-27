package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.request.DemoSpeedRequest;
import serp.project.school_bus_service.application.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.application.dto.response.DemoSessionResponse;

import java.util.List;

public interface IDemoSimulationService {

    DemoSessionResponse startDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse pauseDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse resumeDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse stopDemo(Long tripId, Long tenantId, Long actorId);

    DemoSessionResponse changeSpeed(Long tripId, DemoSpeedRequest request, Long tenantId, Long actorId);

    DemoSessionResponse getState(Long tripId, Long tenantId);

    List<DemoEventLogResponse> getEvents(Long tripId, Long tenantId);
}
