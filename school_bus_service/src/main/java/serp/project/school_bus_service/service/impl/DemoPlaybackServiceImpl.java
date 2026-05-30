package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import serp.project.school_bus_service.dto.domain.PlaybackPosition;
import serp.project.school_bus_service.dto.response.DemoSessionResponse;
import serp.project.school_bus_service.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.enums.DemoEventType;
import serp.project.school_bus_service.enums.DemoSessionStatus;
import serp.project.school_bus_service.repository.DemoSessionRepository;
import serp.project.school_bus_service.service.IDemoEventLogService;
import serp.project.school_bus_service.service.IDemoPlaybackService;
import serp.project.school_bus_service.service.IDemoSessionService;
import serp.project.school_bus_service.service.IDemoWebSocketPublisher;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class DemoPlaybackServiceImpl implements IDemoPlaybackService {

    private final DemoSessionRepository demoSessionRepository;
    private final IDemoSessionService demoSessionService;
    private final IDemoEventLogService demoEventLogService;
    private final IRouteGeometryService routeGeometryService;
    private final IRouteStopService routeStopService;
    private final IDemoWebSocketPublisher webSocketPublisher;

    public DemoPlaybackServiceImpl(DemoSessionRepository demoSessionRepository,
                                   IDemoSessionService demoSessionService,
                                   IDemoEventLogService demoEventLogService,
                                   IRouteGeometryService routeGeometryService,
                                   IRouteStopService routeStopService,
                                   IDemoWebSocketPublisher webSocketPublisher) {
        this.demoSessionRepository = demoSessionRepository;
        this.demoSessionService = demoSessionService;
        this.demoEventLogService = demoEventLogService;
        this.routeGeometryService = routeGeometryService;
        this.routeStopService = routeStopService;
        this.webSocketPublisher = webSocketPublisher;
    }

    @Override
    @Transactional
    public DemoSessionResponse start(Long sessionId, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateTransition(session, DemoSessionStatus.RUNNING);

        session.setStatus(DemoSessionStatus.RUNNING);
        session.setStartedAt(session.getStartedAt() == null ? LocalDateTime.now() : session.getStartedAt());
        session.setPausedAt(null);
        session.setLastEventType(DemoEventType.DEMO_STARTED.name());
        session.markUpdated(actorId.toString());
        demoSessionRepository.save(session);

        var event = demoEventLogService.record(session, DemoEventType.DEMO_STARTED, null, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_STARTED);
        return demoSessionService.toResponse(session);
    }

    @Override
    @Transactional
    public DemoSessionResponse pause(Long sessionId, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateStatus(session, DemoSessionStatus.RUNNING);

        session.setStatus(DemoSessionStatus.PAUSED);
        session.setPausedAt(LocalDateTime.now());
        session.setLastEventType(DemoEventType.DEMO_PAUSED.name());
        session.markUpdated(actorId.toString());
        demoSessionRepository.save(session);

        var event = demoEventLogService.record(session, DemoEventType.DEMO_PAUSED, null, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_PAUSED);
        return demoSessionService.toResponse(session);
    }

    @Override
    @Transactional
    public DemoSessionResponse resume(Long sessionId, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateStatus(session, DemoSessionStatus.PAUSED);

        session.setStatus(DemoSessionStatus.RUNNING);
        session.setPausedAt(null);
        session.setLastEventType(DemoEventType.DEMO_RESUMED.name());
        session.markUpdated(actorId.toString());
        demoSessionRepository.save(session);

        var event = demoEventLogService.record(session, DemoEventType.DEMO_RESUMED, null, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_RESUMED);
        return demoSessionService.toResponse(session);
    }

    @Override
    @Transactional
    public DemoSessionResponse tick(Long sessionId, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateStatus(session, DemoSessionStatus.RUNNING);

        try {
            List<RoutePathCoordinateResponse> path = resolveGeometryPath(session.getTrip());

            // Advance progress
            double currentProgress = session.getProgressPercent() == null ? 0D : session.getProgressPercent();
            double increment = computeTickIncrement(session, path);
            double newProgress = Math.min(100.0, currentProgress + increment);

            session.setProgressPercent(newProgress);
            session.setLastTickAt(LocalDateTime.now());
            session.setLastEventType(DemoEventType.DEMO_TICK.name());

            // Interpolate position along path
            if (!path.isEmpty()) {
                RoutePathCoordinateResponse position = interpolatePosition(path, newProgress / 100.0);
                session.setCurrentLatitude(position.getLatitude());
                session.setCurrentLongitude(position.getLongitude());
            }

            // Determine current stop order based on progress
            updateCurrentStopOrder(session, path);

            // Check if completed
            if (newProgress >= 100.0) {
                session.setStatus(DemoSessionStatus.COMPLETED);
                session.setCompletedAt(LocalDateTime.now());
                session.setProgressPercent(100.0);
                session.setLastEventType(DemoEventType.DEMO_COMPLETED.name());
                session.markUpdated(actorId.toString());
                demoSessionRepository.save(session);
                var event = demoEventLogService.record(session, DemoEventType.DEMO_COMPLETED,
                        buildTickPayload(session), tenantId, actorId);
                webSocketPublisher.publishEvent(session, event);
                webSocketPublisher.publishPosition(session, DemoEventType.DEMO_COMPLETED);
            } else {
                session.markUpdated(actorId.toString());
                demoSessionRepository.save(session);
                var event = demoEventLogService.record(session, DemoEventType.DEMO_TICK,
                        buildTickPayload(session), tenantId, actorId);
                webSocketPublisher.publishEvent(session, event);
                webSocketPublisher.publishPosition(session, DemoEventType.DEMO_TICK);
            }
        } catch (Exception e) {
            log.error("Tick failed for session {}: {}", sessionId, e.getMessage(), e);
            session.setStatus(DemoSessionStatus.ERROR);
            session.setErrorMessage(e.getMessage());
            session.setLastEventType(DemoEventType.DEMO_ERROR.name());
            session.markUpdated(actorId.toString());
            demoSessionRepository.save(session);
            demoEventLogService.record(session, DemoEventType.DEMO_ERROR,
                    "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}", tenantId, actorId);
            webSocketPublisher.publishError(session, e.getMessage());
        }

        return demoSessionService.toResponse(session);
    }

    @Override
    @Transactional
    public DemoSessionResponse stop(Long sessionId, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        if (session.getStatus() == DemoSessionStatus.COMPLETED
                || session.getStatus() == DemoSessionStatus.STOPPED) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, "Session is already terminated");
        }

        session.setStatus(DemoSessionStatus.STOPPED);
        session.setCompletedAt(LocalDateTime.now());
        session.setLastEventType(DemoEventType.DEMO_STOPPED.name());
        session.markUpdated(actorId.toString());
        demoSessionRepository.save(session);

        var event = demoEventLogService.record(session, DemoEventType.DEMO_STOPPED, null, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_STOPPED);
        return demoSessionService.toResponse(session);
    }

    // ─── Jump methods ──────────────────────────────────────────────

    @Override
    @Transactional
    public DemoSessionResponse jumpToStop(Long sessionId, Integer stopOrder, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateJumpAllowed(session);

        PlaybackPosition position = computeJumpToStop(session, stopOrder);
        double fromProgress = session.getProgressPercent() != null ? session.getProgressPercent() : 0;
        Integer fromStopOrder = session.getCurrentStopOrder();

        applyJump(session, position, actorId);

        String payload = buildJumpPayload(fromProgress, position.getProgressPercent(),
                fromStopOrder, stopOrder, position.getLatitude(), position.getLongitude(), "JUMP_TO_STOP");
        var event = demoEventLogService.record(session, DemoEventType.DEMO_JUMPED, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_JUMPED);

        return demoSessionService.toResponse(session);
    }

    @Override
    @Transactional
    public DemoSessionResponse jumpToProgress(Long sessionId, Double progressPercent, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateJumpAllowed(session);

        double clamped = Math.max(0, Math.min(100, progressPercent));
        PlaybackPosition position = computeJumpToProgress(session, clamped);
        double fromProgress = session.getProgressPercent() != null ? session.getProgressPercent() : 0;
        Integer fromStopOrder = session.getCurrentStopOrder();

        applyJump(session, position, actorId);

        String payload = buildJumpPayload(fromProgress, position.getProgressPercent(),
                fromStopOrder, position.getCurrentStopOrder(), position.getLatitude(), position.getLongitude(), "JUMP_TO_PROGRESS");
        var event = demoEventLogService.record(session, DemoEventType.DEMO_JUMPED, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_JUMPED);

        return demoSessionService.toResponse(session);
    }

    @Override
    @Transactional
    public DemoSessionResponse jumpToStart(Long sessionId, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateJumpAllowed(session);

        List<RouteStopEntity> stops = getOrderedStops(session);
        if (stops.isEmpty()) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, "No route stops found for jump");
        }
        Integer firstStopOrder = stops.get(0).getStopOrder();
        PlaybackPosition position = computeJumpToStop(session, firstStopOrder);
        double fromProgress = session.getProgressPercent() != null ? session.getProgressPercent() : 0;
        Integer fromStopOrder = session.getCurrentStopOrder();

        applyJump(session, position, actorId);

        String payload = buildJumpPayload(fromProgress, position.getProgressPercent(),
                fromStopOrder, firstStopOrder, position.getLatitude(), position.getLongitude(), "JUMP_TO_START");
        var event = demoEventLogService.record(session, DemoEventType.DEMO_JUMPED, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_JUMPED);

        return demoSessionService.toResponse(session);
    }

    @Override
    @Transactional
    public DemoSessionResponse jumpToEnd(Long sessionId, Long tenantId, Long actorId) {
        DemoSessionEntity session = demoSessionService.getById(sessionId, tenantId);
        validateJumpAllowed(session);

        List<RouteStopEntity> stops = getOrderedStops(session);
        if (stops.isEmpty()) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, "No route stops found for jump");
        }
        Integer lastStopOrder = stops.get(stops.size() - 1).getStopOrder();
        PlaybackPosition position = computeJumpToStop(session, lastStopOrder);
        double fromProgress = session.getProgressPercent() != null ? session.getProgressPercent() : 0;
        Integer fromStopOrder = session.getCurrentStopOrder();

        applyJump(session, position, actorId);

        String payload = buildJumpPayload(fromProgress, position.getProgressPercent(),
                fromStopOrder, lastStopOrder, position.getLatitude(), position.getLongitude(), "JUMP_TO_END");
        var event = demoEventLogService.record(session, DemoEventType.DEMO_JUMPED, payload, tenantId, actorId);
        webSocketPublisher.publishEvent(session, event);
        webSocketPublisher.publishPosition(session, DemoEventType.DEMO_JUMPED);

        return demoSessionService.toResponse(session);
    }

    // ─── Private helpers ──────────────────────────────────────────────

    private List<RoutePathCoordinateResponse> resolveGeometryPath(TripExecutionEntity trip) {
        // Try to parse route geometry from the trip's stored path
        String geometryJson = trip.getRouteGeometryPath();
        if (StringUtils.hasText(geometryJson)) {
            RoutePathResponse routePath = routeGeometryService.deserialize(geometryJson);
            if (routePath != null && routePath.getCoordinates() != null && !routePath.getCoordinates().isEmpty()) {
                return routePath.getCoordinates();
            }
        }

        // Fallback: use route stop coordinates
        return fallbackToStopCoordinates(trip);
    }

    private List<RoutePathCoordinateResponse> fallbackToStopCoordinates(TripExecutionEntity trip) {
        List<RoutePathCoordinateResponse> coords = new ArrayList<>();
        if (trip.getRoute() != null) {
            List<RouteStopEntity> stops = routeStopService.findByRoute(
                    trip.getRoute().getId(), trip.getTenantId());
            stops.forEach(stop -> {
                Double lat = stop.getLatitude();
                Double lon = stop.getLongitude();
                if (lat != null && lon != null) {
                    coords.add(new RoutePathCoordinateResponse(lat, lon));
                }
            });
        }
        return coords;
    }

    private double computeTickIncrement(DemoSessionEntity session, List<RoutePathCoordinateResponse> path) {
        int speed = session.getSpeedMultiplier() != null ? session.getSpeedMultiplier() : 1;
        Integer duration = session.getDurationSeconds();

        if (duration != null && duration > 0) {
            // Each tick advances by (speed / duration) * 100%
            // Assumption: tick is called once per second of simulated time
            return (speed * 100.0) / duration;
        }

        // Default: divide path into fixed ticks (e.g., 60 ticks total at 1x speed)
        int totalTicks = 60;
        return (speed * 100.0) / totalTicks;
    }

    private RoutePathCoordinateResponse interpolatePosition(List<RoutePathCoordinateResponse> path,
                                                            double fraction) {
        if (path.isEmpty()) return new RoutePathCoordinateResponse(0.0, 0.0);
        if (path.size() == 1 || fraction <= 0) return path.get(0);
        if (fraction >= 1.0) return path.get(path.size() - 1);

        // Compute cumulative distances
        double[] cumDist = new double[path.size()];
        cumDist[0] = 0;
        for (int i = 1; i < path.size(); i++) {
            cumDist[i] = cumDist[i - 1] + haversineKm(
                    path.get(i - 1).getLatitude(), path.get(i - 1).getLongitude(),
                    path.get(i).getLatitude(), path.get(i).getLongitude());
        }

        double totalDist = cumDist[path.size() - 1];
        if (totalDist == 0) return path.get(0);

        double targetDist = fraction * totalDist;

        // Find the segment
        for (int i = 1; i < path.size(); i++) {
            if (cumDist[i] >= targetDist) {
                double segmentStart = cumDist[i - 1];
                double segmentLength = cumDist[i] - cumDist[i - 1];
                double segmentFraction = segmentLength > 0 ? (targetDist - segmentStart) / segmentLength : 0;

                double lat = path.get(i - 1).getLatitude()
                        + segmentFraction * (path.get(i).getLatitude() - path.get(i - 1).getLatitude());
                double lon = path.get(i - 1).getLongitude()
                        + segmentFraction * (path.get(i).getLongitude() - path.get(i - 1).getLongitude());
                return new RoutePathCoordinateResponse(lat, lon);
            }
        }

        return path.get(path.size() - 1);
    }

    private void updateCurrentStopOrder(DemoSessionEntity session, List<RoutePathCoordinateResponse> path) {
        TripExecutionEntity trip = session.getTrip();
        if (trip.getRoute() == null) return;

        List<RouteStopEntity> stops = routeStopService.findByRoute(
                trip.getRoute().getId(), trip.getTenantId());
        if (stops.isEmpty()) return;

        // Determine stop order based on progress fraction and total stops
        double fraction = (session.getProgressPercent() != null ? session.getProgressPercent() : 0) / 100.0;
        int stopIndex = (int) Math.floor(fraction * stops.size());
        stopIndex = Math.min(stopIndex, stops.size() - 1);
        session.setCurrentStopOrder(stops.get(stopIndex).getStopOrder());
    }

    private void validateTransition(DemoSessionEntity session, DemoSessionStatus targetStatus) {
        DemoSessionStatus current = session.getStatus();
        if (targetStatus == DemoSessionStatus.RUNNING) {
            if (current != DemoSessionStatus.READY && current != DemoSessionStatus.PAUSED) {
                throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                        "Cannot start session in status: " + current);
            }
        }
    }

    private void validateStatus(DemoSessionEntity session, DemoSessionStatus expected) {
        if (session.getStatus() != expected) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Expected session status " + expected + " but got " + session.getStatus());
        }
    }

    private String buildTickPayload(DemoSessionEntity session) {
        return "{\"progress\":" + session.getProgressPercent()
                + ",\"lat\":" + session.getCurrentLatitude()
                + ",\"lon\":" + session.getCurrentLongitude()
                + ",\"stopOrder\":" + session.getCurrentStopOrder() + "}";
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

    // ─── Jump helpers ─────────────────────────────────────────────────

    private void validateJumpAllowed(DemoSessionEntity session) {
        DemoSessionStatus status = session.getStatus();
        if (status == DemoSessionStatus.COMPLETED || status == DemoSessionStatus.STOPPED) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Cannot jump in terminated session (status: " + status + ")");
        }
    }

    private List<RouteStopEntity> getOrderedStops(DemoSessionEntity session) {
        TripExecutionEntity trip = session.getTrip();
        if (trip.getRoute() == null) return List.of();
        List<RouteStopEntity> stops = routeStopService.findByRoute(trip.getRoute().getId(), trip.getTenantId());
        stops.sort(Comparator.comparingInt(RouteStopEntity::getStopOrder));
        return stops;
    }

    private PlaybackPosition computeJumpToStop(DemoSessionEntity session, Integer targetStopOrder) {
        List<RouteStopEntity> stops = getOrderedStops(session);
        RouteStopEntity targetStop = stops.stream()
                .filter(s -> s.getStopOrder().equals(targetStopOrder))
                .findFirst()
                .orElseThrow(() -> new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                        "Stop with order " + targetStopOrder + " not found"));

        Double stopLat = targetStop.getLatitude();
        Double stopLon = targetStop.getLongitude();
        if (stopLat == null || stopLon == null) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Stop order " + targetStopOrder + " has no coordinates");
        }

        List<RoutePathCoordinateResponse> path = resolveGeometryPath(session.getTrip());

        if (path.size() >= 2) {
            // Find closest point on polyline to the stop coordinate
            double[] cumDist = computeCumulativeDistances(path);
            double totalDist = cumDist[path.size() - 1];

            double minDist = Double.MAX_VALUE;
            double snapProgress = 0;
            double snapLat = stopLat;
            double snapLon = stopLon;

            for (int i = 0; i < path.size(); i++) {
                double d = haversineKm(stopLat, stopLon,
                        path.get(i).getLatitude(), path.get(i).getLongitude());
                if (d < minDist) {
                    minDist = d;
                    snapProgress = totalDist > 0 ? (cumDist[i] / totalDist) * 100.0 : 0;
                    snapLat = path.get(i).getLatitude();
                    snapLon = path.get(i).getLongitude();
                }
            }

            return new PlaybackPosition(snapLat, snapLon, snapProgress, targetStopOrder, false);
        }

        // Fallback: estimate progress by stop index
        int stopIndex = 0;
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getStopOrder().equals(targetStopOrder)) {
                stopIndex = i;
                break;
            }
        }
        double fallbackProgress = stops.size() > 1 ? (stopIndex * 100.0) / (stops.size() - 1) : 0;
        return new PlaybackPosition(stopLat, stopLon, fallbackProgress, targetStopOrder, true);
    }

    private PlaybackPosition computeJumpToProgress(DemoSessionEntity session, double progressPercent) {
        List<RoutePathCoordinateResponse> path = resolveGeometryPath(session.getTrip());
        double fraction = progressPercent / 100.0;

        Double lat = null;
        Double lon = null;
        boolean fallback = false;

        if (path.size() >= 2) {
            RoutePathCoordinateResponse pos = interpolatePosition(path, fraction);
            lat = pos.getLatitude();
            lon = pos.getLongitude();
        } else {
            fallback = true;
        }

        // Determine stop order at this progress
        List<RouteStopEntity> stops = getOrderedStops(session);
        Integer stopOrder = null;
        if (!stops.isEmpty()) {
            int stopIndex = (int) Math.floor(fraction * stops.size());
            stopIndex = Math.min(stopIndex, stops.size() - 1);
            stopOrder = stops.get(stopIndex).getStopOrder();
            if (lat == null && stops.get(stopIndex).getLatitude() != null) {
                lat = stops.get(stopIndex).getLatitude();
                lon = stops.get(stopIndex).getLongitude();
            }
        }

        return new PlaybackPosition(lat, lon, progressPercent, stopOrder, fallback);
    }

    private void applyJump(DemoSessionEntity session, PlaybackPosition position, Long actorId) {
        session.setProgressPercent(position.getProgressPercent());
        session.setCurrentLatitude(position.getLatitude());
        session.setCurrentLongitude(position.getLongitude());
        session.setCurrentStopOrder(position.getCurrentStopOrder());
        session.setLastTickAt(LocalDateTime.now());
        session.setLastEventType(DemoEventType.DEMO_JUMPED.name());
        session.markUpdated(actorId.toString());
        demoSessionRepository.save(session);
    }

    private double[] computeCumulativeDistances(List<RoutePathCoordinateResponse> path) {
        double[] cumDist = new double[path.size()];
        cumDist[0] = 0;
        for (int i = 1; i < path.size(); i++) {
            cumDist[i] = cumDist[i - 1] + haversineKm(
                    path.get(i - 1).getLatitude(), path.get(i - 1).getLongitude(),
                    path.get(i).getLatitude(), path.get(i).getLongitude());
        }
        return cumDist;
    }

    private String buildJumpPayload(double fromProgress, Double toProgress,
                                    Integer fromStopOrder, Integer targetStopOrder,
                                    Double latitude, Double longitude, String reason) {
        return "{\"fromProgressPercent\":" + fromProgress
                + ",\"toProgressPercent\":" + toProgress
                + ",\"fromStopOrder\":" + fromStopOrder
                + ",\"targetStopOrder\":" + targetStopOrder
                + ",\"latitude\":" + latitude
                + ",\"longitude\":" + longitude
                + ",\"reason\":\"" + reason + "\"}";
    }
}
