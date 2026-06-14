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

import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IMasterDataService;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.service.ISchoolBusDomainNotificationService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;

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

import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.repository.StudentSubscriptionHistoryRepository;
import serp.project.school_bus_service.repository.StudentSubscriptionRepository;

import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class StudentSubscriptionServiceImpl extends AbstractBaseService<StudentSubscriptionEntity, Long>
        implements IStudentSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(StudentSubscriptionServiceImpl.class);

    private final StudentSubscriptionRepository subscriptionRepository;
    private final StudentSubscriptionHistoryRepository historyRepository;
    private final IMasterDataService masterDataService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusDataScopeService schoolBusDataScopeService;
    private final SchoolBusSecurityService securityService;
    private final ISchoolBusDomainNotificationService domainNotificationService;


    public StudentSubscriptionServiceImpl(
            StudentSubscriptionRepository subscriptionRepository,
            StudentSubscriptionHistoryRepository historyRepository,
            IMasterDataService masterDataService,
            ICodeGeneratorService codeGeneratorService,
            SchoolBusMapper mapper,
            MessageCommon messageCommon,
            ISchoolBusDataScopeService schoolBusDataScopeService,
            SchoolBusSecurityService securityService,
            ISchoolBusDomainNotificationService domainNotificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.historyRepository = historyRepository;
        this.masterDataService = masterDataService;
        this.codeGeneratorService = codeGeneratorService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusDataScopeService = schoolBusDataScopeService;
        this.securityService = securityService;
        this.domainNotificationService = domainNotificationService;
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
        // Parent data scope: filter to subscriptions belonging to current parent's students
        if (securityService.isParentOnly()) {
            Long parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("student").get("parentProfile").get("id"), parentProfileId));
        }
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
        schoolBusDataScopeService.assertCanAccessSubscription(id);
        return mapper.toStudentSubscriptionResponse(findById(id, tenantId));
    }

    @Override
    public List<StudentSubscriptionEntity> findAllBySchoolIdAndTenantId(Long schoolId, Long tenantId) {
        return subscriptionRepository.findAllBySchoolIdAndTenantId(schoolId, tenantId);
    }

    @Override
    public StudentSubscriptionEntity getSubscriptionEntity(Long id, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessSubscription(id);
        return findById(id, tenantId);
    }

    @Override
    public List<StudentSubscriptionHistoryResponse> getSubscriptionHistory(Long subscriptionId, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessSubscription(subscriptionId);
        findById(subscriptionId, tenantId);
        return historyRepository.findBySubscriptionIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(subscriptionId, tenantId)
                .stream().map(mapper::toStudentSubscriptionHistoryResponse).toList();
    }

    // ── MANUAL CRUD ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StudentSubscriptionResponse createSubscription(StudentSubscriptionUpsertRequest request, Long tenantId, Long actorId) {
        StudentSubscriptionEntity entity = new StudentSubscriptionEntity();
        entity.markCreated(tenantId, actor(actorId));
        entity.setSubscriptionCode(genCode(tenantId, actorId));
        apply(entity, request, tenantId);
        StudentSubscriptionEntity saved = subscriptionRepository.save(entity);
        domainNotificationService.notifySubscriptionCreated(saved, actorId);
        return mapper.toStudentSubscriptionResponse(saved);
    }

    @Override
    @Transactional
    public StudentSubscriptionResponse updateSubscription(Long id, StudentSubscriptionUpsertRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanAccessSubscription(id);
        StudentSubscriptionEntity entity = findById(id, tenantId);
        entity.markUpdated(actor(actorId));
        apply(entity, request, tenantId);
        StudentSubscriptionEntity saved = subscriptionRepository.save(entity);
        domainNotificationService.notifySubscriptionUpdated(saved, actorId);
        return mapper.toStudentSubscriptionResponse(saved);
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
        // tripOption must have been set by replaceRequestStudents for new requests.
        // The null→ROUND_TRIP fallback is kept only to support RENEW where renewFromApprovedRequest
        // may fill tripOption from the target subscription before calling this method.
        TripOption tripOption = rs.getTripOption() != null ? rs.getTripOption() : TripOption.ROUND_TRIP;
        StudentEntity student = rs.getStudent();

        // Pickup/dropoff must come from the RequestStudent snapshot (set at request create/update time).
        // Do NOT fall back to Student defaults here — that would silently override the snapshot
        // if the student's default pickup/dropoff changed between request creation and approval.
        PickupPointEntity pickup = rs.getPickupPoint();
        if (pickup == null && (tripOption == TripOption.MORNING || tripOption == TripOption.ROUND_TRIP)) {
            // Legacy path: request was created before strict snapshot validation was introduced.
            // Log a warning — approve validation upstream should have already caught this for new requests.
            log.warn("Legacy fallback: RequestStudent {} for student {} has null pickupPoint snapshot " +
                     "(request #{}). This indicates the request was created before snapshot enforcement. " +
                     "Falling back to Student default pickup.", rs.getId(), student.getId(), request.getId());
            pickup = student.getPickupPoint();
            if (pickup == null) {
                throw new AppException(AppErrorCode.Request.INVALID_STATE,
                        "RequestStudent #" + rs.getId() + " is missing a pickup point snapshot and the student "
                                + "has no default pickup point. Edit the request to select a pickup point.");
            }
        }

        PickupPointEntity dropoff = rs.getDropoffPoint();
        if (dropoff == null && (tripOption == TripOption.AFTERNOON || tripOption == TripOption.ROUND_TRIP)) {
            // Legacy path: same rationale as pickup above.
            log.warn("Legacy fallback: RequestStudent {} for student {} has null dropoffPoint snapshot " +
                     "(request #{}). Falling back to Student default or pickup.", rs.getId(), student.getId(), request.getId());
            dropoff = student.getDefaultDropoffPoint();
            if (dropoff == null) {
                dropoff = pickup;
            }
            if (dropoff == null) {
                throw new AppException(AppErrorCode.Request.INVALID_STATE,
                        "RequestStudent #" + rs.getId() + " is missing a drop-off point snapshot and no default can be resolved. "
                                + "Edit the request to select a drop-off point.");
            }
        }

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
        entity.setDropoffPoint(tripOption == TripOption.MORNING ? null : dropoff);
        entity.setTripOption(tripOption);
        copyDays(entity, rs);
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setStatus(SubscriptionStatus.ACTIVE);
        entity.setSourceRequest(request);
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

        target.setStatus(SubscriptionStatus.PAUSED);
        target.markUpdated(actor(actorId));
        subscriptionRepository.save(target);

        recordHistory(target, rs, request, SubscriptionChangeType.PAUSED, oldStatus,
                SubscriptionStatus.PAUSED.name(), actorId, null,
                "Paused by PAUSE_SERVICE request");
    }

    @Override
    @Transactional
    public void resumeFromApprovedRequest(TransportRequestEntity request, RequestStudentEntity rs,
            Long tenantId, Long actorId) {
        StudentSubscriptionEntity target = requireTarget(rs, tenantId);
        String oldStatus = target.getStatus().name();

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
        schoolBusDataScopeService.assertCanAccessSubscription(id);
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

        domainNotificationService.notifySubscriptionStatusChanged(saved, newStatus, actorId);
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
        // Pause period table removed in V31. Always return empty.
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOverlappingPausePeriod(Long subscriptionId, LocalDate pauseFrom, LocalDate pauseTo, Long tenantId) {
        // Pause period table removed in V31.
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveOrScheduledPause(Long subscriptionId, Long tenantId) {
        // Pause period table removed in V31.
        return false;
    }
}
