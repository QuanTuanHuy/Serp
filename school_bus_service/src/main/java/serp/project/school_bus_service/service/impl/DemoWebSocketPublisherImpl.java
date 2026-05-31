package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.message.DemoEventMessage;
import serp.project.school_bus_service.dto.message.DemoPositionMessage;
import serp.project.school_bus_service.entity.DemoEventLogEntity;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.enums.DemoEventType;
import serp.project.school_bus_service.service.IDemoWebSocketPublisher;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DemoWebSocketPublisherImpl implements IDemoWebSocketPublisher {

    private static final String TOPIC_POSITION = "/topic/school-bus/demo-sessions/%d/position";
    private static final String TOPIC_EVENTS = "/topic/school-bus/demo-sessions/%d/events";
    private static final String TOPIC_TRIP_DEMO = "/topic/school-bus/trips/%d/demo";

    private final SimpMessagingTemplate messagingTemplate;

    public DemoWebSocketPublisherImpl(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishPosition(DemoSessionEntity session, DemoEventType eventType) {
        DemoPositionMessage message = buildPositionMessage(session, eventType);
        String sessionTopic = String.format(TOPIC_POSITION, session.getId());
        String tripTopic = String.format(TOPIC_TRIP_DEMO, session.getTrip().getId());

        messagingTemplate.convertAndSend(sessionTopic, message);
        messagingTemplate.convertAndSend(tripTopic, message);

        log.debug("Published demo position: sessionId={}, eventType={}, progress={}%",
                session.getId(), eventType, session.getProgressPercent());
    }

    @Override
    public void publishEvent(DemoSessionEntity session, DemoEventLogEntity event) {
        DemoEventMessage message = buildEventMessage(session, event);
        String topic = String.format(TOPIC_EVENTS, session.getId());

        messagingTemplate.convertAndSend(topic, message);

        log.debug("Published demo event: sessionId={}, eventType={}",
                session.getId(), event.getEventType());
    }

    @Override
    public void publishError(DemoSessionEntity session, String errorMessage) {
        DemoEventMessage message = new DemoEventMessage();
        message.setDemoSessionId(session.getId());
        message.setTripId(session.getTrip().getId());
        message.setEventType(DemoEventType.DEMO_ERROR.name());
        message.setEventTime(LocalDateTime.now());
        message.setPayloadJson("{\"error\":\"" + escapeJson(errorMessage) + "\"}");
        message.setProgressPercent(session.getProgressPercent());
        message.setCurrentLatitude(session.getCurrentLatitude());
        message.setCurrentLongitude(session.getCurrentLongitude());
        message.setCurrentStopOrder(session.getCurrentStopOrder());

        String topic = String.format(TOPIC_EVENTS, session.getId());
        messagingTemplate.convertAndSend(topic, message);

        log.debug("Published demo error: sessionId={}, error={}",
                session.getId(), errorMessage);
    }

    private DemoPositionMessage buildPositionMessage(DemoSessionEntity session, DemoEventType eventType) {
        DemoPositionMessage msg = new DemoPositionMessage();
        msg.setDemoSessionId(session.getId());
        msg.setStatus(session.getStatus().name());
        msg.setProgressPercent(session.getProgressPercent());
        msg.setCurrentLatitude(session.getCurrentLatitude());
        msg.setCurrentLongitude(session.getCurrentLongitude());
        msg.setCurrentStopOrder(session.getCurrentStopOrder());
        msg.setLastTickAt(session.getLastTickAt());
        msg.setLastEventType(session.getLastEventType());
        msg.setEventType(eventType.name());
        msg.setTimestamp(LocalDateTime.now());

        TripExecutionEntity trip = session.getTrip();
        if (trip != null) {
            msg.setTripId(trip.getId());
            msg.setTripCode(trip.getTripCode());
            if (trip.getRoute() != null) {
                msg.setRouteId(trip.getRoute().getId());
                msg.setRouteCode(trip.getRoute().getRouteCode());
            }
        }

        return msg;
    }

    private DemoEventMessage buildEventMessage(DemoSessionEntity session, DemoEventLogEntity event) {
        DemoEventMessage msg = new DemoEventMessage();
        msg.setDemoSessionId(session.getId());
        msg.setTripId(session.getTrip().getId());
        msg.setEventType(event.getEventType().name());
        msg.setEventTime(event.getEventTime());
        msg.setPayloadJson(event.getPayloadJson());
        msg.setProgressPercent(session.getProgressPercent());
        msg.setCurrentLatitude(session.getCurrentLatitude());
        msg.setCurrentLongitude(session.getCurrentLongitude());
        msg.setCurrentStopOrder(session.getCurrentStopOrder());
        return msg;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
