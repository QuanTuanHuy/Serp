package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.DemoEventType;
import serp.project.school_bus_service.enums.DemoSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.TripStopStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.IDemoAutomationService;
import serp.project.school_bus_service.service.IDemoEventLogService;
import serp.project.school_bus_service.service.IDemoWebSocketPublisher;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.service.ITripStopLogService;
import serp.project.school_bus_service.service.ITripStudentService;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class DemoAutomationServiceImpl implements IDemoAutomationService {

    private final ITripExecutionService tripExecutionService;
    private final IAttendanceService attendanceService;
    private final ITripStopLogService tripStopLogService;
    private final ITripStudentService tripStudentService;
    private final IDemoEventLogService demoEventLogService;
    private final IDemoWebSocketPublisher webSocketPublisher;

    public DemoAutomationServiceImpl(ITripExecutionService tripExecutionService,
                                     IAttendanceService attendanceService,
                                     ITripStopLogService tripStopLogService,
                                     ITripStudentService tripStudentService,
                                     IDemoEventLogService demoEventLogService,
                                     IDemoWebSocketPublisher webSocketPublisher) {
        this.tripExecutionService = tripExecutionService;
        this.attendanceService = attendanceService;
        this.tripStopLogService = tripStopLogService;
        this.tripStudentService = tripStudentService;
        this.demoEventLogService = demoEventLogService;
        this.webSocketPublisher = webSocketPublisher;
    }

    @Override
    @Transactional
    public void processAfterTick(DemoSessionEntity session, Long tenantId, Long actorId) {
        // Guard: only process when RUNNING
        if (session.getStatus() != DemoSessionStatus.RUNNING) return;
        if (!Boolean.TRUE.equals(session.getAutoAdvanceStops())) return;
        if (session.getCurrentStopOrder() == null) return;

        TripExecutionEntity trip = session.getTrip();
        if (trip == null) return;

        // Guard: trip must be IN_PROGRESS
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            log.debug("[DemoAutomation] Trip {} not IN_PROGRESS (status={}), skipping automation",
                    trip.getId(), trip.getStatus());
            recordSkipped(session, tenantId, actorId,
                    "Trip not IN_PROGRESS (status: " + trip.getStatus() + ")");
            return;
        }

        Integer currentStopOrder = session.getCurrentStopOrder();

        // Get all trip stop logs ordered by stop_order
        List<TripStopLogEntity> stopLogs = tripStopLogService.findByTrip(trip.getId(), tenantId);
        stopLogs.sort(Comparator.comparingInt(TripStopLogEntity::getStopOrder));

        if (stopLogs.isEmpty()) return;

        // Get students for attendance
        List<TripStudentEntity> students = tripStudentService.findByTrip(trip.getId(), tenantId);
        RouteDirection direction = trip.getRouteDirection();

        // Process all stops up to and including currentStopOrder
        for (TripStopLogEntity stopLog : stopLogs) {
            if (stopLog.getStopOrder() > currentStopOrder) break;

            boolean isBehindBus = stopLog.getStopOrder() < currentStopOrder;

            processStopAutomation(session, trip, stopLog, students, direction,
                    isBehindBus, tenantId, actorId);
        }
    }

    // ─── Stop automation logic ─────────────────────────────────────────

    private void processStopAutomation(DemoSessionEntity session, TripExecutionEntity trip,
                                       TripStopLogEntity stopLog, List<TripStudentEntity> students,
                                       RouteDirection direction, boolean isBehindBus,
                                       Long tenantId, Long actorId) {
        TripStopStatus status = stopLog.getStatus();
        RouteStopEntity routeStop = stopLog.getRouteStop();
        Long routeStopId = routeStop.getId();

        // 1. If stop is PENDING → arrive
        if (status == TripStopStatus.PENDING) {
            try {
                tripExecutionService.arriveStop(trip.getId(), routeStopId, tenantId, actorId);
                recordAutoArrived(session, stopLog, tenantId, actorId);
                // Refresh status after arrive
                status = TripStopStatus.ARRIVED;
            } catch (Exception e) {
                log.warn("[DemoAutomation] Failed to auto-arrive stop order={}: {}",
                        stopLog.getStopOrder(), e.getMessage());
                recordError(session, tenantId, actorId,
                        "Failed to arrive stop " + stopLog.getStopOrder() + ": " + e.getMessage());
                return; // Don't proceed with this stop
            }
        }

        // 2. If stop is ARRIVED/BOARDING → process attendance
        if (status == TripStopStatus.ARRIVED || status == TripStopStatus.BOARDING) {
            if (Boolean.TRUE.equals(session.getAutoAttendance())) {
                processAutoAttendance(session, trip, stopLog, students, direction, tenantId, actorId);
            }

            // 3. If bus has moved past this stop → depart
            if (isBehindBus) {
                try {
                    tripExecutionService.departStop(trip.getId(), routeStopId, tenantId, actorId);
                    recordAutoDeparted(session, stopLog, tenantId, actorId);
                } catch (Exception e) {
                    log.warn("[DemoAutomation] Failed to auto-depart stop order={}: {}",
                            stopLog.getStopOrder(), e.getMessage());
                    recordError(session, tenantId, actorId,
                            "Failed to depart stop " + stopLog.getStopOrder() + ": " + e.getMessage());
                }
            }
        }

        // If already DEPARTED or SKIPPED, skip entirely (idempotent)
    }

    // ─── Attendance automation ─────────────────────────────────────────

    private void processAutoAttendance(DemoSessionEntity session, TripExecutionEntity trip,
                                       TripStopLogEntity stopLog, List<TripStudentEntity> students,
                                       RouteDirection direction, Long tenantId, Long actorId) {
        RouteStopEntity routeStop = stopLog.getRouteStop();
        RouteStopPurpose purpose = routeStop.getStopPurpose();

        // No attendance at DEPOT terminals
        if (isDepotTerminal(purpose, direction)) return;

        if (direction == RouteDirection.OUTBOUND) {
            processOutboundAttendance(session, trip, stopLog, students, purpose, tenantId, actorId);
        } else {
            processReturnAttendance(session, trip, stopLog, students, purpose, tenantId, actorId);
        }
    }

    private void processOutboundAttendance(DemoSessionEntity session, TripExecutionEntity trip,
                                           TripStopLogEntity stopLog, List<TripStudentEntity> students,
                                           RouteStopPurpose purpose, Long tenantId, Long actorId) {
        RouteStopEntity routeStop = stopLog.getRouteStop();

        if (purpose == RouteStopPurpose.PICKUP) {
            // Board PLANNED students whose pickupStop matches this stop
            for (TripStudentEntity ts : students) {
                if (ts.getStatus() != TripStudentStatus.PLANNED) continue;
                if (ts.getPickupStop() == null || !ts.getPickupStop().getId().equals(routeStop.getId())) continue;
                autoBoard(session, trip, ts, routeStop, tenantId, actorId);
            }
        } else if (purpose == RouteStopPurpose.END_TERMINAL) {
            // Dropoff all BOARDED students at school
            for (TripStudentEntity ts : students) {
                if (ts.getStatus() != TripStudentStatus.BOARDED) continue;
                autoDropoff(session, trip, ts, routeStop, tenantId, actorId);
            }
        }
        // START_TERMINAL (depot): no attendance
    }

    private void processReturnAttendance(DemoSessionEntity session, TripExecutionEntity trip,
                                         TripStopLogEntity stopLog, List<TripStudentEntity> students,
                                         RouteStopPurpose purpose, Long tenantId, Long actorId) {
        RouteStopEntity routeStop = stopLog.getRouteStop();

        if (purpose == RouteStopPurpose.START_TERMINAL) {
            // Board all PLANNED students at school
            for (TripStudentEntity ts : students) {
                if (ts.getStatus() != TripStudentStatus.PLANNED) continue;
                autoBoard(session, trip, ts, routeStop, tenantId, actorId);
            }
        } else if (purpose == RouteStopPurpose.DROPOFF) {
            // Dropoff BOARDED students whose dropoffStop matches this stop
            for (TripStudentEntity ts : students) {
                if (ts.getStatus() != TripStudentStatus.BOARDED) continue;
                if (ts.getDropoffStop() == null || !ts.getDropoffStop().getId().equals(routeStop.getId())) continue;
                autoDropoff(session, trip, ts, routeStop, tenantId, actorId);
            }
        }
        // END_TERMINAL (depot): no attendance
    }

    private void autoBoard(DemoSessionEntity session, TripExecutionEntity trip,
                           TripStudentEntity ts, RouteStopEntity routeStop,
                           Long tenantId, Long actorId) {
        try {
            TripAttendanceActionRequest request = new TripAttendanceActionRequest();
            request.setStudentId(ts.getStudent().getId());
            request.setRouteStopId(routeStop.getId());
            request.setNotes("Auto-board by demo automation");
            attendanceService.boardTripStudent(trip.getId(), request, tenantId, actorId);
            recordAutoAttendance(session, ts, routeStop, "BOARD",
                    TripStudentStatus.PLANNED.name(), TripStudentStatus.BOARDED.name(), tenantId, actorId);
        } catch (Exception e) {
            log.warn("[DemoAutomation] Failed to auto-board student {} at stop {}: {}",
                    ts.getStudent().getId(), routeStop.getStopOrder(), e.getMessage());
            recordError(session, tenantId, actorId,
                    "Failed to board student " + ts.getStudent().getId() + ": " + e.getMessage());
        }
    }

    private void autoDropoff(DemoSessionEntity session, TripExecutionEntity trip,
                             TripStudentEntity ts, RouteStopEntity routeStop,
                             Long tenantId, Long actorId) {
        try {
            TripAttendanceActionRequest request = new TripAttendanceActionRequest();
            request.setStudentId(ts.getStudent().getId());
            request.setRouteStopId(routeStop.getId());
            request.setNotes("Auto-dropoff by demo automation");
            attendanceService.dropoffTripStudent(trip.getId(), request, tenantId, actorId);
            recordAutoAttendance(session, ts, routeStop, "DROPOFF",
                    TripStudentStatus.BOARDED.name(), TripStudentStatus.DROPPED_OFF.name(), tenantId, actorId);
        } catch (Exception e) {
            log.warn("[DemoAutomation] Failed to auto-dropoff student {} at stop {}: {}",
                    ts.getStudent().getId(), routeStop.getStopOrder(), e.getMessage());
            recordError(session, tenantId, actorId,
                    "Failed to dropoff student " + ts.getStudent().getId() + ": " + e.getMessage());
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    private boolean isDepotTerminal(RouteStopPurpose purpose, RouteDirection direction) {
        if (direction == RouteDirection.OUTBOUND) {
            return purpose == RouteStopPurpose.START_TERMINAL; // Depot is start for OUTBOUND
        } else {
            return purpose == RouteStopPurpose.END_TERMINAL; // Depot is end for RETURN
        }
    }

    // ─── Event recording ───────────────────────────────────────────────

    private void recordAutoArrived(DemoSessionEntity session, TripStopLogEntity stopLog,
                                   Long tenantId, Long actorId) {
        String payload = "{\"tripId\":" + session.getTrip().getId()
                + ",\"demoSessionId\":" + session.getId()
                + ",\"stopOrder\":" + stopLog.getStopOrder()
                + ",\"stopId\":" + stopLog.getRouteStop().getId()
                + ",\"action\":\"ARRIVE\""
                + ",\"reason\":\"AUTO_ADVANCE_STOPS\"}";
        var event = demoEventLogService.record(session, DemoEventType.DEMO_AUTO_ARRIVED_STOP, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
    }

    private void recordAutoDeparted(DemoSessionEntity session, TripStopLogEntity stopLog,
                                    Long tenantId, Long actorId) {
        String payload = "{\"tripId\":" + session.getTrip().getId()
                + ",\"demoSessionId\":" + session.getId()
                + ",\"stopOrder\":" + stopLog.getStopOrder()
                + ",\"stopId\":" + stopLog.getRouteStop().getId()
                + ",\"action\":\"DEPART\""
                + ",\"reason\":\"AUTO_ADVANCE_STOPS\"}";
        var event = demoEventLogService.record(session, DemoEventType.DEMO_AUTO_DEPARTED_STOP, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
    }

    private void recordAutoAttendance(DemoSessionEntity session, TripStudentEntity ts,
                                      RouteStopEntity routeStop, String action,
                                      String fromStatus, String toStatus,
                                      Long tenantId, Long actorId) {
        String studentName = ts.getStudent() != null ? ts.getStudent().getFullName() : "unknown";
        String payload = "{\"tripId\":" + session.getTrip().getId()
                + ",\"demoSessionId\":" + session.getId()
                + ",\"stopOrder\":" + routeStop.getStopOrder()
                + ",\"stopId\":" + routeStop.getId()
                + ",\"action\":\"" + action + "\""
                + ",\"studentId\":" + ts.getStudent().getId()
                + ",\"studentName\":\"" + escapeJson(studentName) + "\""
                + ",\"fromStatus\":\"" + fromStatus + "\""
                + ",\"toStatus\":\"" + toStatus + "\""
                + ",\"reason\":\"AUTO_ATTENDANCE\"}";
        var event = demoEventLogService.record(session, DemoEventType.DEMO_AUTO_ATTENDANCE, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
    }

    private void recordSkipped(DemoSessionEntity session, Long tenantId, Long actorId, String reason) {
        String payload = "{\"demoSessionId\":" + session.getId()
                + ",\"reason\":\"" + escapeJson(reason) + "\"}";
        var event = demoEventLogService.record(session, DemoEventType.DEMO_AUTOMATION_SKIPPED, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
    }

    private void recordError(DemoSessionEntity session, Long tenantId, Long actorId, String message) {
        String payload = "{\"demoSessionId\":" + session.getId()
                + ",\"error\":\"" + escapeJson(message) + "\"}";
        var event = demoEventLogService.record(session, DemoEventType.DEMO_AUTOMATION_ERROR, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
