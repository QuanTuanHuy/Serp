package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.message.TripOperationEventMessage;

public interface ITripWebSocketPublisher {
    void publish(TripOperationEventMessage message);
}
