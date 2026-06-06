package serp.project.school_bus_service.service;

import serp.project.school_bus_service.entity.DemoSessionEntity;

/**
 * Handles demo automation: auto arrive/depart stops and auto attendance
 * when the demo bus moves to new positions during tick or jump.
 */
public interface IDemoAutomationService {

    /**
     * Process automation after a tick or jump updates the demo session position.
     * Checks autoAdvanceStops / autoAttendance flags and processes accordingly.
     */
    void processAfterTick(DemoSessionEntity session, Long tenantId, Long actorId);
}
