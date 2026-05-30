package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.DemoSessionResponse;
import serp.project.school_bus_service.entity.DemoSessionEntity;

public interface IDemoSessionService {

    DemoSessionEntity createFromTrip(Long tripId, Integer durationSeconds,
                                     Boolean autoAdvanceStops, Boolean autoAttendance,
                                     Long tenantId, Long actorId);

    DemoSessionEntity getById(Long sessionId, Long tenantId);

    DemoSessionEntity getByTripId(Long tripId, Long tenantId);

    DemoSessionResponse toResponse(DemoSessionEntity session);
}
