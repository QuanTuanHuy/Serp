package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.DemoSessionResponse;

public interface IDemoPlaybackService {

    DemoSessionResponse start(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse pause(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse resume(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse tick(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse stop(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse jumpToStop(Long sessionId, Integer stopOrder, Long tenantId, Long actorId);

    DemoSessionResponse jumpToProgress(Long sessionId, Double progressPercent, Long tenantId, Long actorId);

    DemoSessionResponse jumpToStart(Long sessionId, Long tenantId, Long actorId);

    DemoSessionResponse jumpToEnd(Long sessionId, Long tenantId, Long actorId);
}
