package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.request.DemoSpeedRequest;
import serp.project.school_bus_service.application.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.application.dto.response.DemoSessionResponse;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IDemoSimulationService;
import serp.project.school_bus_service.core.service.ITripExecutionService;
import serp.project.school_bus_service.enums.DemoSessionStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.DemoEventLogEntity;
import serp.project.school_bus_service.infrastructure.store.model.DemoSessionEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripExecutionEntity;
import serp.project.school_bus_service.infrastructure.store.repository.DemoEventLogRepository;
import serp.project.school_bus_service.infrastructure.store.repository.DemoSessionRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoSimulationServiceImpl implements IDemoSimulationService {

    private final DemoSessionRepository demoSessionRepository;
    private final DemoEventLogRepository demoEventLogRepository;
    private final ITripExecutionService tripExecutionService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;

    @Override
    @Transactional
    public DemoSessionResponse startDemo(Long tripId, Long tenantId, Long actorId) {
        DemoSessionEntity session = getOrCreateSession(tripId, tenantId, actorId);
        session.setStatus(DemoSessionStatus.RUNNING);
        session.setStartedAt(session.getStartedAt() == null ? LocalDateTime.now() : session.getStartedAt());
        session.setPausedAt(null);
        session.setProgressPercent(session.getProgressPercent() == null ? 0D : session.getProgressPercent());
        session.markUpdated(actor(actorId));
        DemoSessionEntity saved = demoSessionRepository.save(session);
        record(saved, "START", tenantId, actorId, null);
        return response(saved, tenantId);
    }

    @Override
    @Transactional
    public DemoSessionResponse pauseDemo(Long tripId, Long tenantId, Long actorId) {
        DemoSessionEntity session = getSession(tripId, tenantId);
        session.setStatus(DemoSessionStatus.PAUSED);
        session.setPausedAt(LocalDateTime.now());
        session.markUpdated(actor(actorId));
        DemoSessionEntity saved = demoSessionRepository.save(session);
        record(saved, "PAUSE", tenantId, actorId, null);
        return response(saved, tenantId);
    }

    @Override
    @Transactional
    public DemoSessionResponse resumeDemo(Long tripId, Long tenantId, Long actorId) {
        DemoSessionEntity session = getSession(tripId, tenantId);
        session.setStatus(DemoSessionStatus.RUNNING);
        session.setPausedAt(null);
        session.markUpdated(actor(actorId));
        DemoSessionEntity saved = demoSessionRepository.save(session);
        record(saved, "RESUME", tenantId, actorId, null);
        return response(saved, tenantId);
    }

    @Override
    @Transactional
    public DemoSessionResponse stopDemo(Long tripId, Long tenantId, Long actorId) {
        DemoSessionEntity session = getSession(tripId, tenantId);
        session.setStatus(DemoSessionStatus.STOPPED);
        session.setCompletedAt(LocalDateTime.now());
        session.markUpdated(actor(actorId));
        DemoSessionEntity saved = demoSessionRepository.save(session);
        record(saved, "STOP", tenantId, actorId, null);
        return response(saved, tenantId);
    }

    @Override
    @Transactional
    public DemoSessionResponse changeSpeed(Long tripId, DemoSpeedRequest request, Long tenantId, Long actorId) {
        DemoSessionEntity session = getSession(tripId, tenantId);
        session.setSpeedMultiplier(request.getSpeedMultiplier());
        session.markUpdated(actor(actorId));
        DemoSessionEntity saved = demoSessionRepository.save(session);
        record(saved, "SPEED", tenantId, actorId, "{\"speedMultiplier\":" + request.getSpeedMultiplier() + "}");
        return response(saved, tenantId);
    }

    @Override
    public DemoSessionResponse getState(Long tripId, Long tenantId) {
        return response(getSession(tripId, tenantId), tenantId);
    }

    @Override
    public List<DemoEventLogResponse> getEvents(Long tripId, Long tenantId) {
        DemoSessionEntity session = getSession(tripId, tenantId);
        return demoEventLogRepository
                .findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeDesc(session.getId(), tenantId)
                .stream()
                .map(mapper::toDemoEventLogResponse)
                .toList();
    }

    private DemoSessionEntity getOrCreateSession(Long tripId, Long tenantId, Long actorId) {
        return demoSessionRepository.findFirstByTripIdAndTenantIdAndIsDeletedFalseOrderByIdDesc(tripId, tenantId)
                .orElseGet(() -> {
                    TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
                    DemoSessionEntity session = new DemoSessionEntity();
                    session.markCreated(tenantId, actor(actorId));
                    session.setDemoCode(codeGeneratorService.generate(SchoolBusCode.DEMO.sequenceKey(),
                            SchoolBusCode.DEMO.prefix(), tenantId, actorId));
                    session.setTrip(trip);
                    session.setStatus(DemoSessionStatus.READY);
                    session.setSpeedMultiplier(1);
                    session.setProgressPercent(0D);
                    return demoSessionRepository.save(session);
                });
    }

    private DemoSessionEntity getSession(Long tripId, Long tenantId) {
        return demoSessionRepository.findFirstByTripIdAndTenantIdAndIsDeletedFalseOrderByIdDesc(tripId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Demo session not found"));
    }

    private DemoSessionResponse response(DemoSessionEntity session, Long tenantId) {
        return mapper.toDemoSessionResponse(session, demoEventLogRepository
                .findByDemoSessionIdAndTenantIdAndIsDeletedFalseOrderByEventTimeDesc(session.getId(), tenantId));
    }

    private void record(DemoSessionEntity session, String eventType, Long tenantId, Long actorId, String payloadJson) {
        DemoEventLogEntity event = new DemoEventLogEntity();
        event.markCreated(tenantId, actor(actorId));
        event.setDemoSession(session);
        event.setEventType(eventType);
        event.setEventTime(LocalDateTime.now());
        event.setPayloadJson(payloadJson);
        demoEventLogRepository.save(event);
    }

    private String actor(Long actorId) {
        return actorId == null ? "SYSTEM" : String.valueOf(actorId);
    }
}
