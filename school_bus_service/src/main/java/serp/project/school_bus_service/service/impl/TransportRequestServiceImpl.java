package serp.project.school_bus_service.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.TransportRequestParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.request.RejectRequest;
import serp.project.school_bus_service.dto.request.RequestStudentItemRequest;
import serp.project.school_bus_service.dto.request.TransportRequestUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.RequestStudentResponse;
import serp.project.school_bus_service.dto.response.TransportRequestDetailResponse;
import serp.project.school_bus_service.dto.response.TransportRequestHistoryResponse;
import serp.project.school_bus_service.dto.response.TransportRequestResponse;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IMasterDataService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RequestSource;
import serp.project.school_bus_service.enums.RequestType;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.RequestStudentEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.TransportRequestHistoryEntity;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.repository.RequestStudentRepository;
import serp.project.school_bus_service.repository.TransportRequestHistoryRepository;
import serp.project.school_bus_service.repository.TransportRequestRepository;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import serp.project.school_bus_service.enums.SubscriptionStatus;

@Service
public class TransportRequestServiceImpl extends AbstractBaseService<TransportRequestEntity, Long>
        implements ITransportRequestService {

    private final TransportRequestRepository transportRequestRepository;
    private final RequestStudentRepository requestStudentRepository;
    private final TransportRequestHistoryRepository transportRequestHistoryRepository;
    private final IMasterDataService masterDataService;
    private final IStudentSubscriptionService subscriptionService;
    private final ISchoolScheduleService schoolScheduleService;
    private final ISchoolPickupPointService schoolPickupPointService;
    private final ISchoolPickupPointWindowService windowService;
    private final ICodeGeneratorService codeGeneratorService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;


    public TransportRequestServiceImpl(
    TransportRequestRepository transportRequestRepository,
                                 RequestStudentRepository requestStudentRepository,
                                 TransportRequestHistoryRepository transportRequestHistoryRepository,
                                 IMasterDataService masterDataService,
                                 IStudentSubscriptionService subscriptionService,
                                 ISchoolScheduleService schoolScheduleService,
                                 ISchoolPickupPointService schoolPickupPointService,
                                 ISchoolPickupPointWindowService windowService,
                                 ICodeGeneratorService codeGeneratorService,
                                 IAuditLogService auditLogService,
                                 SchoolBusMapper mapper,
                                 MessageCommon messageCommon) {
        this.transportRequestRepository = transportRequestRepository;
        this.requestStudentRepository = requestStudentRepository;
        this.transportRequestHistoryRepository = transportRequestHistoryRepository;
        this.masterDataService = masterDataService;
        this.subscriptionService = subscriptionService;
        this.schoolScheduleService = schoolScheduleService;
        this.schoolPickupPointService = schoolPickupPointService;
        this.windowService = windowService;
        this.codeGeneratorService = codeGeneratorService;
        this.auditLogService = auditLogService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<TransportRequestEntity, Long> getRepository() {
        return transportRequestRepository;
    }

    @Override
    public PageResponse<TransportRequestResponse> getTransportRequests(TransportRequestParamsRequest params, Long tenantId) {
        return PageResponse.from(transportRequestRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "parentProfile.fullName", "school.name",
                        "requestType", "status", "notes"),
                pageable(params, Set.of("id", "requestType", "status", "effectiveFrom", "effectiveTo", "createdAt",
                        "updatedAt"), "createdAt")),
                mapper::toTransportRequestResponse);
    }

    @Override
    public TransportRequestDetailResponse getTransportRequest(Long id, Long tenantId) {
        TransportRequestEntity request = findById(transportRequestRepository, id, tenantId);
        return mapper.toTransportRequestDetailResponse(request,
                requestStudentRepository.findByRequestIdAndTenantIdAndIsDeletedFalse(id, tenantId));
    }

    @Override
    public List<RequestStudentResponse> getRequestStudents(Long requestId, Long tenantId) {
        findById(transportRequestRepository, requestId, tenantId);
        return requestStudentRepository.findByRequestIdAndTenantIdAndIsDeletedFalse(requestId, tenantId).stream()
                .map(mapper::toRequestStudentResponse)
                .toList();
    }

    @Override
    public List<TransportRequestHistoryResponse> getTransportRequestHistory(Long requestId, Long tenantId) {
        findById(transportRequestRepository, requestId, tenantId);
        return transportRequestHistoryRepository
                .findByRequestIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(requestId, tenantId)
                .stream()
                .map(mapper::toTransportRequestHistoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public TransportRequestResponse createTransportRequest(TransportRequestUpsertRequest request, Long tenantId, Long actorId) {
        TransportRequestEntity entity = new TransportRequestEntity();
        entity.markCreated(tenantId, actor(actorId));
        applyTransportRequest(entity, request, tenantId);
        entity.setRequestCode(generateCode(SchoolBusCode.REQUEST, tenantId, actorId));
        entity.setRequestedAt(LocalDateTime.now());
        entity.setRequestSource(RequestSource.ADMIN);
        entity.setChangeReason(request.getChangeReason());
        entity.setStatus(RequestStatus.SUBMITTED);
        entity.setApprovedAt(null);
        entity.setApprovedBy(null);
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        replaceRequestStudents(saved, request.getStudents(), tenantId, actorId);
        recordHistory(saved, null, RequestStatus.SUBMITTED, actorId, null, "Created transport request");
        auditLogService.log(tenantId, actorId, "TransportRequest", saved.getId(), "CREATE", "Created transport request");
        return mapper.toTransportRequestResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse updateTransportRequest(Long id, TransportRequestUpsertRequest request, Long tenantId,
            Long actorId) {
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() == RequestStatus.APPROVED || entity.getStatus() == RequestStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Request.CANNOT_EDIT, messageCommon.getMessage(AppErrorCode.Request.CANNOT_EDIT, entity.getStatus()));
        }
        entity.markUpdated(actor(actorId));
        RequestStatus oldStatus = entity.getStatus();
        applyTransportRequest(entity, request, tenantId);
        entity.setChangeReason(request.getChangeReason());
        // If rejected, re-submit on update
        if (oldStatus == RequestStatus.REJECTED) {
            entity.setStatus(RequestStatus.SUBMITTED);
            entity.setRejectionReason(null);
        }
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        replaceRequestStudents(saved, request.getStudents(), tenantId, actorId);
        recordHistory(saved, oldStatus, saved.getStatus(), actorId, request.getChangeReason(), "Updated transport request");
        auditLogService.log(tenantId, actorId, "TransportRequest", saved.getId(), "UPDATE", "Updated transport request");
        return mapper.toTransportRequestResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse approveTransportRequest(Long id, Long tenantId, Long actorId) {
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() != RequestStatus.SUBMITTED) {
            throw new AppException(AppErrorCode.Request.ONLY_SUBMITTED_APPROVED, messageCommon.getMessage(AppErrorCode.Request.ONLY_SUBMITTED_APPROVED));
        }

        entity.markUpdated(actor(actorId));
        RequestStatus oldStatus = entity.getStatus();
        entity.setStatus(RequestStatus.APPROVED);
        entity.setApprovedBy(actorId);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);

        // Dispatch approve to subscription service based on request type
        List<RequestStudentEntity> requestStudents = requestStudentRepository
                .findByRequestIdAndTenantIdAndIsDeletedFalse(saved.getId(), tenantId);

        for (RequestStudentEntity requestStudent : requestStudents) {
            dispatchApprove(saved, requestStudent, tenantId, actorId);
        }

        recordHistory(saved, oldStatus, RequestStatus.APPROVED, actorId, null,
                "Approved transport request (" + saved.getRequestType().name() + ")");
        auditLogService.log(tenantId, actorId, "TransportRequest", saved.getId(), "APPROVE",
                "Approved " + saved.getRequestType().name());
        return mapper.toTransportRequestResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse rejectTransportRequest(Long id, RejectRequest request, Long tenantId, Long actorId) {
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() != RequestStatus.SUBMITTED) {
            throw new AppException(AppErrorCode.Request.ONLY_SUBMITTED_REJECTED, messageCommon.getMessage(AppErrorCode.Request.ONLY_SUBMITTED_REJECTED));
        }

        entity.markUpdated(actor(actorId));
        RequestStatus oldStatus = entity.getStatus();
        entity.setStatus(RequestStatus.REJECTED);
        entity.setRejectionReason(request.getReason());
        entity.setApprovedBy(null);
        entity.setApprovedAt(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        recordHistory(saved, oldStatus, RequestStatus.REJECTED, actorId, request.getReason(), "Rejected transport request");
        auditLogService.log(tenantId, actorId, "TransportRequest", saved.getId(), "REJECT", "Rejected transport request");
        return mapper.toTransportRequestResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse cancelTransportRequest(Long id, Long tenantId, Long actorId) {
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() != RequestStatus.SUBMITTED && entity.getStatus() != RequestStatus.DRAFT) {
            throw new AppException(AppErrorCode.Request.INVALID_STATE,
                    messageCommon.getMessage(AppErrorCode.Request.INVALID_STATE));
        }

        entity.markUpdated(actor(actorId));
        RequestStatus oldStatus = entity.getStatus();
        entity.setStatus(RequestStatus.CANCELLED);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        recordHistory(saved, oldStatus, RequestStatus.CANCELLED, actorId, null, "Cancelled transport request");
        auditLogService.log(tenantId, actorId, "TransportRequest", saved.getId(), "CANCEL", "Cancelled transport request");
        return mapper.toTransportRequestResponse(saved);
    }

    @Override
    public boolean hasApprovedRequestForStudent(Long studentId, Long schoolId, LocalDate serviceDate, Long tenantId) {
        return requestStudentRepository.findByStudentIdAndTenantIdAndIsDeletedFalse(studentId, tenantId).stream()
                .map(RequestStudentEntity::getRequest)
                .filter(request -> request.getSchool().getId().equals(schoolId))
                .filter(request -> request.getStatus() == RequestStatus.APPROVED)
                .anyMatch(request -> !serviceDate.isBefore(request.getEffectiveFrom())
                        && (request.getEffectiveTo() == null || !serviceDate.isAfter(request.getEffectiveTo())));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void dispatchApprove(TransportRequestEntity request, RequestStudentEntity requestStudent,
            Long tenantId, Long actorId) {
        switch (request.getRequestType()) {
            case NEW_SERVICE -> {
                StudentSubscriptionEntity sub = subscriptionService.createFromApprovedRequest(
                        request, requestStudent, tenantId, actorId);
                // Link created subscription back to request student
                requestStudent.setSubscription(sub);
                requestStudentRepository.save(requestStudent);
            }
            case CHANGE_SERVICE -> subscriptionService.changeFromApprovedRequest(request, requestStudent, tenantId, actorId);
            case STOP_SERVICE -> subscriptionService.stopFromApprovedRequest(request, requestStudent, tenantId, actorId);
            case PAUSE_SERVICE -> subscriptionService.pauseFromApprovedRequest(request, requestStudent, tenantId, actorId);
            case RESUME_SERVICE -> subscriptionService.resumeFromApprovedRequest(request, requestStudent, tenantId, actorId);
            case RENEW_SERVICE -> {
                StudentSubscriptionEntity sub = subscriptionService.renewFromApprovedRequest(
                        request, requestStudent, tenantId, actorId);
                requestStudent.setSubscription(sub);
                requestStudentRepository.save(requestStudent);
            }
        }
    }

    private void applyTransportRequest(TransportRequestEntity entity, TransportRequestUpsertRequest request, Long tenantId) {
        entity.setParentProfile(masterDataService.getParent(request.getParentProfileId(), tenantId));
        entity.setSchool(masterDataService.getSchool(request.getSchoolId(), tenantId));
        entity.setRequestType(RequestType.parse(request.getRequestType()));
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setNotes(request.getNotes());
        entity.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void replaceRequestStudents(TransportRequestEntity entity, List<RequestStudentItemRequest> students, Long tenantId,
            Long actorId) {
        requestStudentRepository.softDeleteByRequestId(entity.getId(), tenantId, actor(actorId));
        if (students == null) return;

        Long schoolId = entity.getSchool().getId();
        RequestType requestType = entity.getRequestType();
        LocalDate requestFrom = entity.getEffectiveFrom();
        LocalDate requestTo   = entity.getEffectiveTo();

        for (RequestStudentItemRequest studentRequest : students) {

            // ── Load entities ────────────────────────────────────────────────
            RequestStudentEntity rs = new RequestStudentEntity();
            rs.markCreated(tenantId, actor(actorId));
            rs.setRequest(entity);
            rs.setStudent(masterDataService.getStudent(studentRequest.getStudentId(), tenantId));

            // ── Schedule validation ─────────────────────────────────────────
            SchoolScheduleEntity schedule = null;
            Set<String> scheduleDays = Set.of();
            if (studentRequest.getSchoolScheduleId() != null) {
                // Load schedule with days for day subset validation
                schedule = schoolScheduleService.getScheduleWithDays(studentRequest.getSchoolScheduleId(), tenantId);

                // Rule 2a: schedule must belong to the selected school
                if (!schedule.getSchool().getId().equals(schoolId)) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Schedule #" + studentRequest.getSchoolScheduleId()
                                    + " does not belong to the selected school");
                }

                // Rule 2b: request effective range must overlap schedule effective range
                if (requestFrom != null) {
                    LocalDate schedFrom = schedule.getEffectiveFrom();
                    LocalDate schedTo   = schedule.getEffectiveTo() != null ? schedule.getEffectiveTo() : LocalDate.MAX;
                    LocalDate reqTo     = requestTo != null ? requestTo : LocalDate.MAX;
                    if (requestFrom.isAfter(schedTo) || reqTo.isBefore(schedFrom)) {
                        throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                                "Request effective range [" + requestFrom + " – " + (requestTo != null ? requestTo : "∞")
                                        + "] does not overlap schedule effective range ["
                                        + schedFrom + " – " + (schedule.getEffectiveTo() != null ? schedule.getEffectiveTo() : "∞") + "]");
                    }
                }

                // Build schedule days set (upper-cased)
                scheduleDays = schedule.getScheduleDays().stream()
                        .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted()))
                        .map(d -> d.getDayOfWeek().toUpperCase())
                        .collect(Collectors.toSet());

                rs.setSchoolSchedule(schedule);
            }

            // ── Days of week validation ─────────────────────────────────────
            boolean mon = Boolean.TRUE.equals(studentRequest.getMonday());
            boolean tue = Boolean.TRUE.equals(studentRequest.getTuesday());
            boolean wed = Boolean.TRUE.equals(studentRequest.getWednesday());
            boolean thu = Boolean.TRUE.equals(studentRequest.getThursday());
            boolean fri = Boolean.TRUE.equals(studentRequest.getFriday());
            boolean sat = Boolean.TRUE.equals(studentRequest.getSaturday());
            boolean sun = Boolean.TRUE.equals(studentRequest.getSunday());

            // At least 1 day required
            if (!mon && !tue && !wed && !thu && !fri && !sat && !sun) {
                throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                        "At least one day of week must be selected for student #" + studentRequest.getStudentId());
            }

            // Rule 1: selected days must be a subset of schedule days (if schedule chosen)
            final Set<String> finalScheduleDays = scheduleDays;
            if (schedule != null && !finalScheduleDays.isEmpty()) {
                Map<String, Boolean> selectedDayMap = Map.of(
                        "MONDAY", mon, "TUESDAY", tue, "WEDNESDAY", wed,
                        "THURSDAY", thu, "FRIDAY", fri, "SATURDAY", sat, "SUNDAY", sun);
                selectedDayMap.forEach((dayName, selected) -> {
                    if (selected && !finalScheduleDays.contains(dayName)) {
                        throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                                "Day " + dayName + " is not part of schedule #"
                                        + studentRequest.getSchoolScheduleId() + " for student #"
                                        + studentRequest.getStudentId());
                    }
                });
            }

            rs.setMonday(mon);
            rs.setTuesday(tue);
            rs.setWednesday(wed);
            rs.setThursday(thu);
            rs.setFriday(fri);
            rs.setSaturday(sat);
            rs.setSunday(sun);

            // ── TripOption validation ───────────────────────────────────────
            TripOption opt = TripOption.parseNullable(studentRequest.getTripOption());
            rs.setTripOption(opt);

            if (opt != null) {
                boolean needsPickup  = opt == TripOption.MORNING || opt == TripOption.ROUND_TRIP;
                boolean needsDropoff = opt == TripOption.AFTERNOON || opt == TripOption.ROUND_TRIP;

                // Rule 3: pickup/dropoff required per tripOption
                if (needsPickup && studentRequest.getPickupPointId() == null) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Trip option " + opt + " requires a pickup point for student #" + studentRequest.getStudentId());
                }
                if (needsDropoff && studentRequest.getDropoffPointId() == null) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Trip option " + opt + " requires a drop-off point for student #" + studentRequest.getStudentId());
                }
            }

            // ── Pickup point validation ────────────────────────────────────
            if (studentRequest.getPickupPointId() != null) {
                // Rule 4a: must be linked to school (active)
                if (!schoolPickupPointService.isPickupPointLinkedToSchool(schoolId, studentRequest.getPickupPointId(), tenantId)) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Pickup point #" + studentRequest.getPickupPointId() + " is not linked to the selected school");
                }
                // Rule 4b: usageType must allow PICKUP
                schoolPickupPointService.findLinkBySchoolAndPickupPoint(schoolId, studentRequest.getPickupPointId(), tenantId)
                        .ifPresent(spp -> {
                            String ut = spp.getPickupPoint().getUsageType();
                            if (ut != null && !ut.equals("PICKUP_ONLY") && !ut.equals("PICKUP_DROPOFF")) {
                                throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                                        "Pickup point #" + studentRequest.getPickupPointId()
                                                + " has usageType '" + ut + "' which does not support PICKUP");
                            }
                        });

                rs.setPickupPoint(masterDataService.getPickupPoint(studentRequest.getPickupPointId(), tenantId));

                // Rule 5: warn if no PICKUP_TO_SCHOOL window configured (non-blocking)
                final SchoolScheduleEntity pickupSchedule = schedule;
                if (pickupSchedule != null) {
                    schoolPickupPointService.findLinkBySchoolAndPickupPoint(schoolId, studentRequest.getPickupPointId(), tenantId)
                            .ifPresent(spp -> {
                                if (!windowService.hasWindow(spp.getId(), pickupSchedule.getId(), "PICKUP_TO_SCHOOL", tenantId)) {
                                    // Non-blocking: log warning only – window may be configured later
                                    auditLogService.log(tenantId, actorId, "TransportRequest", entity.getId(),
                                            "WARN", "No PICKUP_TO_SCHOOL window configured for pickup point #"
                                                    + studentRequest.getPickupPointId() + " + schedule #" + pickupSchedule.getId());
                                }
                            });
                }
            } else {
                rs.setPickupPoint(null);
            }

            // ── Dropoff point validation ────────────────────────────────────
            if (studentRequest.getDropoffPointId() != null) {
                // Rule 4a: must be linked to school (active)
                if (!schoolPickupPointService.isPickupPointLinkedToSchool(schoolId, studentRequest.getDropoffPointId(), tenantId)) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Drop-off point #" + studentRequest.getDropoffPointId() + " is not linked to the selected school");
                }
                // Rule 4b: usageType must allow DROPOFF
                schoolPickupPointService.findLinkBySchoolAndPickupPoint(schoolId, studentRequest.getDropoffPointId(), tenantId)
                        .ifPresent(spp -> {
                            String ut = spp.getPickupPoint().getUsageType();
                            if (ut != null && !ut.equals("DROPOFF_ONLY") && !ut.equals("PICKUP_DROPOFF")) {
                                throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                                        "Drop-off point #" + studentRequest.getDropoffPointId()
                                                + " has usageType '" + ut + "' which does not support DROPOFF");
                            }
                        });

                rs.setDropoffPoint(masterDataService.getPickupPoint(studentRequest.getDropoffPointId(), tenantId));

                // Rule 5: warn if no DROPOFF_FROM_SCHOOL window configured (non-blocking)
                final SchoolScheduleEntity dropoffSchedule = schedule;
                if (dropoffSchedule != null) {
                    schoolPickupPointService.findLinkBySchoolAndPickupPoint(schoolId, studentRequest.getDropoffPointId(), tenantId)
                            .ifPresent(spp -> {
                                if (!windowService.hasWindow(spp.getId(), dropoffSchedule.getId(), "DROPOFF_FROM_SCHOOL", tenantId)) {
                                    auditLogService.log(tenantId, actorId, "TransportRequest", entity.getId(),
                                            "WARN", "No DROPOFF_FROM_SCHOOL window configured for drop-off point #"
                                                    + studentRequest.getDropoffPointId() + " + schedule #" + dropoffSchedule.getId());
                                }
                            });
                }
            } else {
                rs.setDropoffPoint(null);
            }

            // ── Target subscription validation ─────────────────────────────
            // Rule 6: non-NEW_SERVICE request types must have a targetSubscriptionId
            boolean requiresTarget = requestType != RequestType.NEW_SERVICE;
            if (requiresTarget) {
                if (studentRequest.getTargetSubscriptionId() == null) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Request type " + requestType + " requires a target subscription for student #"
                                    + studentRequest.getStudentId());
                }
                StudentSubscriptionEntity target = subscriptionService.getSubscriptionEntity(
                        studentRequest.getTargetSubscriptionId(), tenantId);

                // Must belong to same student
                if (!target.getStudent().getId().equals(rs.getStudent().getId())) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Target subscription #" + studentRequest.getTargetSubscriptionId()
                                    + " does not belong to student #" + studentRequest.getStudentId());
                }
                // Must belong to same school
                if (!target.getSchool().getId().equals(schoolId)) {
                    throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                            "Target subscription #" + studentRequest.getTargetSubscriptionId()
                                    + " does not belong to the selected school");
                }
                // STOPPED / EXPIRED cannot be reactivated directly
                SubscriptionStatus ts = target.getStatus();
                if (requestType == RequestType.PAUSE_SERVICE
                        || requestType == RequestType.RESUME_SERVICE
                        || requestType == RequestType.STOP_SERVICE
                        || requestType == RequestType.CHANGE_SERVICE) {
                    if (ts == SubscriptionStatus.STOPPED
                            || ts == SubscriptionStatus.EXPIRED) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Cannot apply " + requestType + " to a " + ts + " subscription. "
                                        + "Use RENEW_SERVICE or NEW_SERVICE to restart service.");
                    }
                }

                rs.setTargetSubscription(target);
            } else if (studentRequest.getTargetSubscriptionId() != null) {
                // NEW_SERVICE: ignore target if accidentally sent, but still allow setting
                rs.setTargetSubscription(subscriptionService.getSubscriptionEntity(
                        studentRequest.getTargetSubscriptionId(), tenantId));
            }

            rs.setStudentNote(studentRequest.getStudentNote());
            requestStudentRepository.save(rs);
        }
    }

    private Specification<TransportRequestEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }

    private void recordHistory(TransportRequestEntity request, RequestStatus oldStatus, RequestStatus newStatus,
            Long actorId, String reason, String notes) {
        TransportRequestHistoryEntity history = new TransportRequestHistoryEntity();
        history.markCreated(request.getTenantId(), actor(actorId));
        history.setRequest(request);
        history.setOldStatus(oldStatus == null ? null : oldStatus.name());
        history.setNewStatus(newStatus.name());
        history.setChangedBy(actorId);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(reason);
        history.setNotes(notes);
        transportRequestHistoryRepository.save(history);
    }

    private String generateCode(SchoolBusCode code, Long tenantId, Long actorId) {
        return codeGeneratorService.generate(code.sequenceKey(), code.prefix(), tenantId, actorId);
    }

    @Override
    public List<RequestStudentEntity> findApprovedManifestStudents(Long schoolId, LocalDate serviceDate, Long tenantId) {
        return requestStudentRepository.findApprovedManifestBySchoolAndServiceDate(
                schoolId, serviceDate, tenantId, RequestStatus.APPROVED);
    }

    @Override
    public long countByTenant(Long tenantId) {
        return transportRequestRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }

    @Override
    public long countByTenantAndStatus(Long tenantId, RequestStatus status) {
        return transportRequestRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, status);
    }

    @Override
    public long countBySchoolAndTenant(Long schoolId, Long tenantId) {
        return transportRequestRepository.countBySchoolIdAndTenantIdAndIsDeletedFalse(schoolId, tenantId);
    }
}
