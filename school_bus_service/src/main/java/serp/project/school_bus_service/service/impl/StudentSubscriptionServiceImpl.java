package serp.project.school_bus_service.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.StudentSubscriptionParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.request.StudentSubscriptionUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.StudentSubscriptionHistoryResponse;
import serp.project.school_bus_service.dto.response.StudentSubscriptionResponse;
import serp.project.school_bus_service.dto.response.SubscriptionPausePeriodResponse;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IMasterDataService;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.enums.PausePeriodStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.SubscriptionChangeType;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.RequestStudentEntity;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionHistoryEntity;
import serp.project.school_bus_service.entity.SubscriptionPausePeriodEntity;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.repository.StudentSubscriptionHistoryRepository;
import serp.project.school_bus_service.repository.StudentSubscriptionRepository;
import serp.project.school_bus_service.repository.SubscriptionPausePeriodRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class StudentSubscriptionServiceImpl extends AbstractBaseService<StudentSubscriptionEntity, Long>
        implements IStudentSubscriptionService {

    private final StudentSubscriptionRepository subscriptionRepository;
    private final StudentSubscriptionHistoryRepository historyRepository;
    private final SubscriptionPausePeriodRepository pausePeriodRepository;
    private final IMasterDataService masterDataService;
    private final ICodeGeneratorService codeGeneratorService;
    private final ISchoolScheduleService schoolScheduleService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;


    public StudentSubscriptionServiceImpl(
    StudentSubscriptionRepository subscriptionRepository,
                                 StudentSubscriptionHistoryRepository historyRepository,
                                 SubscriptionPausePeriodRepository pausePeriodRepository,
                                 IMasterDataService masterDataService,
                                 ICodeGeneratorService codeGeneratorService,
                                 ISchoolScheduleService schoolScheduleService,
                                 SchoolBusMapper mapper,
                                 MessageCommon messageCommon) {
        this.subscriptionRepository = subscriptionRepository;
        this.historyRepository = historyRepository;
        this.pausePeriodRepository = pausePeriodRepository;
        this.masterDataService = masterDataService;
        this.codeGeneratorService = codeGeneratorService;
        this.schoolScheduleService = schoolScheduleService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<StudentSubscriptionEntity, Long> getRepository() {
        return subscriptionRepository;
    }

    // ── LIST / GET ──────────────────────────────────────────────────────────

    @Override
    public PageResponse<StudentSubscriptionResponse> getSubscriptions(StudentSubscriptionParamsRequest params, Long tenantId) {
        Specification<StudentSubscriptionEntity> spec = spec(tenantId, params == null ? null : params.getKeyword(),
                "subscriptionCode", "student.fullName", "school.name", "status", "tripOption");
        if (params != null && params.getSchoolId() != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("school").get("id"), params.getSchoolId()));
        if (params != null && params.getStudentId() != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("student").get("id"), params.getStudentId()));
        if (params != null && params.getStatus() != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), SubscriptionStatus.parse(params.getStatus())));
        if (params != null && params.getTripOption() != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("tripOption"), TripOption.parse(params.getTripOption())));
        return PageResponse.from(subscriptionRepository.findAll(spec,
                pageable(params, Set.of("id", "subscriptionCode", "effectiveFrom", "effectiveTo", "status",
                        "createdAt", "updatedAt"), "createdAt")),
                mapper::toStudentSubscriptionResponse);
    }

    @Override
    public StudentSubscriptionResponse getSubscription(Long id, Long tenantId) {
        return mapper.toStudentSubscriptionResponse(findById(id, tenantId));
    }

    @Override
    public StudentSubscriptionEntity getSubscriptionEntity(Long id, Long tenantId) {
        return findById(id, tenantId);
    }

    @Override
    public List<StudentSubscriptionHistoryResponse> getSubscriptionHistory(Long subscriptionId, Long tenantId) {
        findById(subscriptionId, tenantId);
        return historyRepository.findBySubscriptionIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(subscriptionId, tenantId)
                .stream().map(mapper::toStudentSubscriptionHistoryResponse).toList();
    }

    @Override
    public List<SubscriptionPausePeriodResponse> getSubscriptionPausePeriods(Long subscriptionId, Long tenantId) {
        findById(subscriptionId, tenantId);
        return pausePeriodRepository.findBySubscriptionIdAndTenantIdAndIsDeletedFalseOrderByPauseFromDesc(subscriptionId, tenantId)
                .stream().map(mapper::toSubscriptionPausePeriodResponse).toList();
    }

    // ── MANUAL CRUD ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StudentSubscriptionResponse createSubscription(StudentSubscriptionUpsertRequest request, Long tenantId, Long actorId) {
        StudentSubscriptionEntity entity = new StudentSubscriptionEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSubscriptionCode(genCode(tenantId, actorId));
        apply(entity, request, tenantId);
        return mapper.toStudentSubscriptionResponse(subscriptionRepository.save(entity));
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse updateSubscription(Long id, StudentSubscriptionUpsertRequest request, Long tenantId, Long actorId) {
        StudentSubscriptionEntity entity = findById(id, tenantId);
        entity.markUpdated(actor(actorId));
        apply(entity, request, tenantId);
        return mapper.toStudentSubscriptionResponse(subscriptionRepository.save(entity));
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse activateSubscription(Long id, Long tenantId, Long actorId) {
        return transition(id, tenantId, actorId, SubscriptionStatus.ACTIVE);
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse pauseSubscription(Long id, Long tenantId, Long actorId) {
        return transition(id, tenantId, actorId, SubscriptionStatus.PAUSED);
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse stopSubscription(Long id, Long tenantId, Long actorId) {
        return transition(id, tenantId, actorId, SubscriptionStatus.STOPPED);
    }

    @Override
    public List<StudentSubscriptionEntity> findEligibleSubscriptions(Long schoolId, RouteDirection direction,
            LocalDate serviceDate, Long tenantId) {
        boolean isOutbound = direction == RouteDirection.OUTBOUND;
        int dayIndex = serviceDate.getDayOfWeek().getValue();
        List<TripOption> allowedTrips = isOutbound
                ? List.of(TripOption.MORNING, TripOption.ROUND_TRIP)
                : List.of(TripOption.AFTERNOON, TripOption.ROUND_TRIP);
        return subscriptionRepository.findEligibleForPlanning(
                schoolId, tenantId, serviceDate, dayIndex, allowedTrips, isOutbound);
    }

    // ── APPROVE WORKFLOWS ───────────────────────────────────────────────────

    @Override
    @Transactional
    public StudentSubscriptionEntity createFromApprovedRequest(TransportRequestEntity request,
            RequestStudentEntity rs, Long tenantId, Long actorId) {
        TripOption tripOption = rs.getTripOption() != null ? rs.getTripOption() : TripOption.ROUND_TRIP;
        StudentEntity student = rs.getStudent();
        PickupPointEntity pickup = rs.getPickupPoint() != null ? rs.getPickupPoint() : student.getPickupPoint();
        PickupPointEntity dropoff = rs.getDropoffPoint();

        if (subscriptionRepository.existsOverlappingActiveSubscription(student.getId(), tripOption,
                request.getEffectiveFrom(), request.getEffectiveTo(), tenantId, null)) {
            throw new AppException(AppErrorCode.Subscription.OVERLAP, messageCommon.getMessage(AppErrorCode.Subscription.OVERLAP));
        }

        StudentSubscriptionEntity entity = new StudentSubscriptionEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSubscriptionCode(genCode(tenantId, actorId));
        entity.setStudent(student);
        entity.setSchool(request.getSchool());
        entity.setPickupPoint(tripOption == TripOption.AFTERNOON ? null : pickup);
        entity.setDropoffPoint(tripOption == TripOption.MORNING ? null : (dropoff != null ? dropoff : pickup));
        entity.setTripOption(tripOption);
        copyDays(entity, rs);
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setStatus(SubscriptionStatus.ACTIVE);
        entity.setSourceRequest(request);
        entity.setSchoolSchedule(rs.getSchoolSchedule());
        entity.setIsActive(Boolean.TRUE);
        StudentSubscriptionEntity saved = subscriptionRepository.save(entity);

        recordHistory(saved, rs, request, SubscriptionChangeType.CREATED, null,
                SubscriptionStatus.ACTIVE.name(), actorId, null, "Created from approved NEW_SERVICE request");
        return saved;
    }

    @Override
    @Transactional
    public void changeFromApprovedRequest(TransportRequestEntity request, RequestStudentEntity rs,
            Long tenantId, Long actorId) {
        StudentSubscriptionEntity target = requireTarget(rs, tenantId);
        String oldStatus = target.getStatus().name();

        // Close old subscription
        target.setStatus(SubscriptionStatus.STOPPED);
        target.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
        target.markUpdated(actor(actorId));
        subscriptionRepository.save(target);
        recordHistory(target, rs, request, SubscriptionChangeType.CHANGED, oldStatus,
                SubscriptionStatus.STOPPED.name(), actorId, null, "Closed by CHANGE_SERVICE request");

        // Create new subscription from snapshot
        StudentSubscriptionEntity newSub = createFromApprovedRequest(request, rs, tenantId, actorId);
        // Override the history type for the new one
        recordHistory(newSub, rs, request, SubscriptionChangeType.CHANGED, null,
                SubscriptionStatus.ACTIVE.name(), actorId, null, "Created by CHANGE_SERVICE (replaces " + target.getSubscriptionCode() + ")");
    }

    @Override
    @Transactional
    public void stopFromApprovedRequest(TransportRequestEntity request, RequestStudentEntity rs,
            Long tenantId, Long actorId) {
        StudentSubscriptionEntity target = requireTarget(rs, tenantId);
        String oldStatus = target.getStatus().name();
        target.setStatus(SubscriptionStatus.STOPPED);
        target.setEffectiveTo(request.getEffectiveFrom());
        target.markUpdated(actor(actorId));
        subscriptionRepository.save(target);
        recordHistory(target, rs, request, SubscriptionChangeType.STOPPED, oldStatus,
                SubscriptionStatus.STOPPED.name(), actorId, null, "Stopped by STOP_SERVICE request");
    }

    @Override
    @Transactional
    public void pauseFromApprovedRequest(TransportRequestEntity request, RequestStudentEntity rs,
            Long tenantId, Long actorId) {
        StudentSubscriptionEntity target = requireTarget(rs, tenantId);
        String oldStatus = target.getStatus().name();

        SubscriptionPausePeriodEntity pause = new SubscriptionPausePeriodEntity();
        pause.markCreated(tenantId, actor(actorId));
        pause.setSubscription(target);
        pause.setSourceRequest(request);
        pause.setRequestStudent(rs);
        pause.setPauseFrom(request.getEffectiveFrom());
        pause.setPauseTo(request.getEffectiveTo());
        pause.setReason(request.getNotes());
        boolean startsNow = !request.getEffectiveFrom().isAfter(LocalDate.now());
        pause.setStatus(startsNow ? PausePeriodStatus.ACTIVE : PausePeriodStatus.SCHEDULED);
        pausePeriodRepository.save(pause);

        if (startsNow) {
            target.setStatus(SubscriptionStatus.PAUSED);
            target.markUpdated(actor(actorId));
            subscriptionRepository.save(target);
        }
        recordHistory(target, rs, request, SubscriptionChangeType.PAUSED, oldStatus,
                startsNow ? SubscriptionStatus.PAUSED.name() : oldStatus, actorId, null,
                "Paused by PAUSE_SERVICE request" + (startsNow ? "" : " (scheduled)"));
    }

    @Override
    @Transactional
    public void resumeFromApprovedRequest(TransportRequestEntity request, RequestStudentEntity rs,
            Long tenantId, Long actorId) {
        StudentSubscriptionEntity target = requireTarget(rs, tenantId);
        String oldStatus = target.getStatus().name();

        // Cancel or complete active/scheduled pause periods
        for (SubscriptionPausePeriodEntity pp : pausePeriodRepository.findBySubscriptionIdAndStatusIn(
                target.getId(), tenantId, List.of(PausePeriodStatus.ACTIVE, PausePeriodStatus.SCHEDULED))) {
            pp.setStatus(PausePeriodStatus.CANCELLED);
            pp.markUpdated(actor(actorId));
            pausePeriodRepository.save(pp);
        }

        target.setStatus(SubscriptionStatus.ACTIVE);
        target.markUpdated(actor(actorId));
        subscriptionRepository.save(target);
        recordHistory(target, rs, request, SubscriptionChangeType.RESUMED, oldStatus,
                SubscriptionStatus.ACTIVE.name(), actorId, null, "Resumed by RESUME_SERVICE request");
    }

    @Override
    @Transactional
    public StudentSubscriptionEntity renewFromApprovedRequest(TransportRequestEntity request,
            RequestStudentEntity rs, Long tenantId, Long actorId) {
        // Target is the old subscription to trace lineage
        StudentSubscriptionEntity target = rs.getTargetSubscription() != null
                ? findById(rs.getTargetSubscription().getId(), tenantId)
                : null;

        // Fill null snapshot fields from target if available
        if (target != null) {
            if (rs.getTripOption() == null) rs.setTripOption(target.getTripOption());
            if (rs.getPickupPoint() == null) rs.setPickupPoint(target.getPickupPoint());
            if (rs.getDropoffPoint() == null) rs.setDropoffPoint(target.getDropoffPoint());
            if (rs.getSchoolSchedule() == null) rs.setSchoolSchedule(target.getSchoolSchedule());
        }

        StudentSubscriptionEntity newSub = createFromApprovedRequest(request, rs, tenantId, actorId);
        recordHistory(newSub, rs, request, SubscriptionChangeType.RENEWED, null,
                SubscriptionStatus.ACTIVE.name(), actorId, null,
                "Renewed by RENEW_SERVICE" + (target != null ? " (from " + target.getSubscriptionCode() + ")" : ""));
        return newSub;
    }

    // ── PRIVATE HELPERS ─────────────────────────────────────────────────────

    private StudentSubscriptionEntity requireTarget(RequestStudentEntity rs, Long tenantId) {
        if (rs.getTargetSubscription() == null) {
            throw new AppException(AppErrorCode.Subscription.TARGET_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Subscription.TARGET_REQUIRED));
        }
        return findById(rs.getTargetSubscription().getId(), tenantId);
    }

    private void recordHistory(StudentSubscriptionEntity sub, RequestStudentEntity rs,
            TransportRequestEntity request, SubscriptionChangeType changeType,
            String oldStatus, String newStatus, Long actorId, String reason, String notes) {
        StudentSubscriptionHistoryEntity h = new StudentSubscriptionHistoryEntity();
        h.markCreated(sub.getTenantId(), actor(actorId));
        h.setSubscription(sub);
        h.setSourceRequest(request);
        h.setRequestStudent(rs);
        h.setChangeType(changeType);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setChangedBy(actorId);
        h.setChangedAt(LocalDateTime.now());
        h.setReason(reason);
        h.setNotes(notes);
        // Snapshot key fields
        h.setNewPickupPointId(sub.getPickupPoint() != null ? sub.getPickupPoint().getId() : null);
        h.setNewDropoffPointId(sub.getDropoffPoint() != null ? sub.getDropoffPoint().getId() : null);
        h.setNewSchoolScheduleId(sub.getSchoolSchedule() != null ? sub.getSchoolSchedule().getId() : null);
        h.setNewTripOption(sub.getTripOption() != null ? sub.getTripOption().name() : null);
        h.setNewEffectiveFrom(sub.getEffectiveFrom());
        h.setNewEffectiveTo(sub.getEffectiveTo());
        historyRepository.save(h);
    }

    private void copyDays(StudentSubscriptionEntity entity, RequestStudentEntity rs) {
        entity.setMonday(rs.getMonday() != null ? rs.getMonday() : Boolean.TRUE);
        entity.setTuesday(rs.getTuesday() != null ? rs.getTuesday() : Boolean.TRUE);
        entity.setWednesday(rs.getWednesday() != null ? rs.getWednesday() : Boolean.TRUE);
        entity.setThursday(rs.getThursday() != null ? rs.getThursday() : Boolean.TRUE);
        entity.setFriday(rs.getFriday() != null ? rs.getFriday() : Boolean.TRUE);
        entity.setSaturday(rs.getSaturday() != null ? rs.getSaturday() : Boolean.FALSE);
        entity.setSunday(rs.getSunday() != null ? rs.getSunday() : Boolean.FALSE);
    }

    private StudentSubscriptionResponse transition(Long id, Long tenantId, Long actorId, SubscriptionStatus newStatus) {
        StudentSubscriptionEntity entity = findById(id, tenantId);
        SubscriptionStatus oldStatus = entity.getStatus();
        if (oldStatus == newStatus) {
            throw new AppException(AppErrorCode.Subscription.INVALID_STATE,
                    messageCommon.getMessage(AppErrorCode.Subscription.INVALID_STATE));
        }

        // Block: STOPPED subscriptions cannot be reactivated manually;
        // they must go through NEW_SERVICE / RENEW_SERVICE request workflow.
        if (oldStatus == SubscriptionStatus.STOPPED && newStatus == SubscriptionStatus.ACTIVE) {
            throw new AppException(AppErrorCode.Subscription.INVALID_STATE,
                    messageCommon.getMessage(AppErrorCode.Subscription.INVALID_STATE));
        }

        entity.setStatus(newStatus);
        entity.markUpdated(actor(actorId));
        StudentSubscriptionEntity saved = subscriptionRepository.save(entity);

        // Map transition → change type (no ACTIVATED in enum)
        SubscriptionChangeType changeType = switch (newStatus) {
            case ACTIVE -> SubscriptionChangeType.RESUMED;   // only reachable from PAUSED→ACTIVE
            case PAUSED -> SubscriptionChangeType.PAUSED;
            case STOPPED -> SubscriptionChangeType.STOPPED;
            default -> SubscriptionChangeType.CHANGED;
        };

        // Record history (no request context for manual transitions)
        recordHistory(saved, null, null, changeType,
                oldStatus.name(), newStatus.name(), actorId, null,
                "Manual " + changeType.name().toLowerCase() + " by admin");

        return mapper.toStudentSubscriptionResponse(saved);
    }

    private void apply(StudentSubscriptionEntity entity, StudentSubscriptionUpsertRequest request, Long tenantId) {
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom()))
            throw new AppException(AppErrorCode.Subscription.INVALID_REQUEST, messageCommon.getMessage(AppErrorCode.Subscription.INVALID_REQUEST));
        StudentEntity student = masterDataService.getStudent(request.getStudentId(), tenantId);
        if (!student.getSchool().getId().equals(request.getSchoolId()))
            throw new AppException(AppErrorCode.Subscription.INVALID_REQUEST, messageCommon.getMessage(AppErrorCode.Subscription.INVALID_REQUEST));
        TripOption tripOption = TripOption.parse(request.getTripOption());
        SubscriptionStatus status = request.getStatus() == null ? SubscriptionStatus.ACTIVE : SubscriptionStatus.parse(request.getStatus());
        if (status == SubscriptionStatus.ACTIVE && subscriptionRepository.existsOverlappingActiveSubscription(
                student.getId(), tripOption, request.getEffectiveFrom(), request.getEffectiveTo(), tenantId, entity.getId()))
            throw new AppException(AppErrorCode.Subscription.OVERLAP, messageCommon.getMessage(AppErrorCode.Subscription.OVERLAP));
        entity.setStudent(student);
        entity.setSchool(student.getSchool());
        entity.setPickupPoint(resolvePoint(request.getPickupPointId(), tenantId));
        entity.setDropoffPoint(resolvePoint(request.getDropoffPointId(), tenantId));
        entity.setTripOption(tripOption);
        entity.setMonday(Boolean.TRUE.equals(request.getMonday()));
        entity.setTuesday(Boolean.TRUE.equals(request.getTuesday()));
        entity.setWednesday(Boolean.TRUE.equals(request.getWednesday()));
        entity.setThursday(Boolean.TRUE.equals(request.getThursday()));
        entity.setFriday(Boolean.TRUE.equals(request.getFriday()));
        entity.setSaturday(Boolean.TRUE.equals(request.getSaturday()));
        entity.setSunday(Boolean.TRUE.equals(request.getSunday()));
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setStatus(status);
        entity.setIsActive(request.resolveIsActive(Boolean.TRUE));
        if (request.getSchoolScheduleId() != null)
            entity.setSchoolSchedule(schoolScheduleService.getSchedule(request.getSchoolScheduleId(), tenantId));
        else
            entity.setSchoolSchedule(null);
    }

    private PickupPointEntity resolvePoint(Long pointId, Long tenantId) {
        return pointId == null ? null : masterDataService.getPickupPoint(pointId, tenantId);
    }

    private boolean servesDirection(StudentSubscriptionEntity e, RouteDirection dir) {
        if (e.getTripOption() == TripOption.ROUND_TRIP) return true;
        return dir == RouteDirection.OUTBOUND ? e.getTripOption() == TripOption.MORNING : e.getTripOption() == TripOption.AFTERNOON;
    }

    private boolean servesDate(StudentSubscriptionEntity e, LocalDate d) {
        DayOfWeek day = d.getDayOfWeek();
        return switch (day) {
            case MONDAY -> Boolean.TRUE.equals(e.getMonday());
            case TUESDAY -> Boolean.TRUE.equals(e.getTuesday());
            case WEDNESDAY -> Boolean.TRUE.equals(e.getWednesday());
            case THURSDAY -> Boolean.TRUE.equals(e.getThursday());
            case FRIDAY -> Boolean.TRUE.equals(e.getFriday());
            case SATURDAY -> Boolean.TRUE.equals(e.getSaturday());
            case SUNDAY -> Boolean.TRUE.equals(e.getSunday());
        };
    }

    private Specification<StudentSubscriptionEntity> spec(Long t, String kw, String... f) {
        return BaseSpecification.tenantActiveWithKeyword(t, kw, f);
    }

    private Pageable pageable(BaseParamsRequest p, Set<String> s, String d) { return PageableUtils.from(p, s, d); }

    private String genCode(Long tenantId, Long actorId) {
        return codeGeneratorService.generate(SchoolBusCode.SUBSCRIPTION.sequenceKey(), SchoolBusCode.SUBSCRIPTION.prefix(), tenantId, actorId);
    }

    @Override
    public List<StudentSubscriptionEntity> findEligibleForPlanning(
            Long schoolId, Long tenantId, LocalDate serviceDate,
            int dayIndex, List<TripOption> allowedTripOptions, boolean isOutbound) {
        return subscriptionRepository.findEligibleForPlanning(
                schoolId, tenantId, serviceDate, dayIndex, allowedTripOptions, isOutbound);
    }

    @Override
    public List<Long> findPausedSubscriptionIds(List<Long> subscriptionIds, Long tenantId, LocalDate serviceDate) {
        if (subscriptionIds == null || subscriptionIds.isEmpty()) {
            return List.of();
        }
        return pausePeriodRepository.findPausedSubscriptionIds(subscriptionIds, tenantId, serviceDate);
    }
}
