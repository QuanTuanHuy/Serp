package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.DemoSessionResponse;
import serp.project.school_bus_service.entity.DemoEventLogEntity;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.enums.DemoEventType;
import serp.project.school_bus_service.enums.DemoSessionStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.DemoSessionRepository;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IDemoEventLogService;
import serp.project.school_bus_service.service.IDemoSessionService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

import java.util.List;

@Service
public class DemoSessionServiceImpl implements IDemoSessionService {

    private final DemoSessionRepository demoSessionRepository;
    private final IDemoEventLogService demoEventLogService;
    private final ITripExecutionService tripExecutionService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;

    public DemoSessionServiceImpl(DemoSessionRepository demoSessionRepository,
                                  IDemoEventLogService demoEventLogService,
                                  ITripExecutionService tripExecutionService,
                                  ICodeGeneratorService codeGeneratorService,
                                  SchoolBusMapper mapper) {
        this.demoSessionRepository = demoSessionRepository;
        this.demoEventLogService = demoEventLogService;
        this.tripExecutionService = tripExecutionService;
        this.codeGeneratorService = codeGeneratorService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public DemoSessionEntity createFromTrip(Long tripId, Integer durationSeconds,
                                            Boolean autoAdvanceStops, Boolean autoAttendance,
                                            Long tenantId, Long actorId) {
        // Return existing active session if one exists (READY/RUNNING/PAUSED)
        List<DemoSessionStatus> activeStatuses = List.of(
                DemoSessionStatus.READY, DemoSessionStatus.RUNNING, DemoSessionStatus.PAUSED);
        var existing = demoSessionRepository.findFirstByTripIdAndTenantIdAndStatusInAndIsDeletedFalseOrderByIdDesc(
                tripId, tenantId, activeStatuses);
        if (existing.isPresent()) {
            return existing.get();
        }

        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);

        DemoSessionEntity session = new DemoSessionEntity();
        session.markCreated(tenantId, actorId.toString());
        session.setDemoCode(codeGeneratorService.generate(
                SchoolBusCode.DEMO.sequenceKey(), SchoolBusCode.DEMO.prefix(), tenantId, actorId));
        session.setTrip(trip);
        session.setStatus(DemoSessionStatus.READY);
        session.setSpeedMultiplier(1);
        session.setProgressPercent(0D);
        session.setDurationSeconds(durationSeconds);
        session.setAutoAdvanceStops(autoAdvanceStops != null ? autoAdvanceStops : Boolean.FALSE);
        session.setAutoAttendance(autoAttendance != null ? autoAttendance : Boolean.FALSE);

        return demoSessionRepository.save(session);
    }

    @Override
    public DemoSessionEntity getById(Long sessionId, Long tenantId) {
        return demoSessionRepository.findByIdAndTenantIdAndIsDeletedFalse(sessionId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Demo session not found"));
    }

    @Override
    public DemoSessionEntity getByTripId(Long tripId, Long tenantId) {
        return demoSessionRepository.findFirstByTripIdAndTenantIdAndIsDeletedFalseOrderByIdDesc(tripId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Demo session not found for trip"));
    }

    @Override
    public DemoSessionResponse toResponse(DemoSessionEntity session) {
        List<DemoEventLogEntity> events = demoEventLogService.getEventEntities(
                session.getId(), session.getTenantId());
        return mapper.toDemoSessionResponse(session, events);
    }
}
