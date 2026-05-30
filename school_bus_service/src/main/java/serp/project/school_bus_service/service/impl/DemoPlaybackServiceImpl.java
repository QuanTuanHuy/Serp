package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

    public DemoPlaybackServiceImpl(DemoSessionRepository demoSessionRepository,
                                   IDemoSessionService demoSessionService,
                                   IDemoEventLogService demoEventLogService,
                                   IRouteGeometryService routeGeometryService,
                                   IRouteStopService routeStopService) {
        this.demoSessionRepository = demoSessionRepository;
        this.demoSessionService = demoSessionService;
        this.demoEventLogService = demoEventLogService;
        this.routeGeometryService = routeGeometryService;
        this.routeStopService = routeStopService;
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

        demoEventLogService.record(session, DemoEventType.DEMO_STARTED, null, tenantId, actorId);
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

        demoEventLogService.record(session, DemoEventType.DEMO_PAUSED, null, tenantId, actorId);
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

        demoEventLogService.record(session, DemoEventType.DEMO_RESUMED, null, tenantId, actorId);
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
                demoEventLogService.record(session, DemoEventType.DEMO_COMPLETED,
                        buildTickPayload(session), tenantId, actorId);
            } else {
                session.markUpdated(actorId.toString());
                demoSessionRepository.save(session);
                demoEventLogService.record(session, DemoEventType.DEMO_TICK,
                        buildTickPayload(session), tenantId, actorId);
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

        demoEventLogService.record(session, DemoEventType.DEMO_STOPPED, null, tenantId, actorId);
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
}
