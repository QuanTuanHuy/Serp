package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.DemoSessionResponse;

public interface IDemoPlaybackService {

    DemoSessionResponse start(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse pause(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse resume(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse tick(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse stop(Long sessionId, Long tenantId, Long actorId);
}
