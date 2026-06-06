package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.message.TripOperationEventMessage;
import serp.project.school_bus_service.service.ITripWebSocketPublisher;

@Service
@Slf4j
public class TripWebSocketPublisherImpl implements ITripWebSocketPublisher {

    private static final String TOPIC_TRIP_EVENTS = "/topic/school-bus/trips/%d/events";
    private static final String TOPIC_ALL_TRIP_EVENTS = "/topic/school-bus/trips/events";

    private final SimpMessagingTemplate messagingTemplate;

    public TripWebSocketPublisherImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(TripOperationEventMessage message) {
        if (message == null || message.getTripId() == null) {
            return;
        }

        String tripTopic = String.format(TOPIC_TRIP_EVENTS, message.getTripId());
        try {
            messagingTemplate.convertAndSend(tripTopic, message);
            messagingTemplate.convertAndSend(TOPIC_ALL_TRIP_EVENTS, message);
            log.info("Published trip operation event: tripId={}, action={}, eventType={}",
                    message.getTripId(), message.getAction(), message.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish trip operation event via WebSocket: {}", e.getMessage(), e);
        }
    }
}
