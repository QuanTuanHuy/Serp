package serp.project.school_bus_service.service;

import serp.project.school_bus_service.entity.DemoEventLogEntity;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.enums.DemoEventType;

public interface IDemoWebSocketPublisher {

    void publishPosition(DemoSessionEntity session, DemoEventType eventType);

    void publishEvent(DemoSessionEntity session, DemoEventLogEntity event);

    void publishError(DemoSessionEntity session, String message);
}
