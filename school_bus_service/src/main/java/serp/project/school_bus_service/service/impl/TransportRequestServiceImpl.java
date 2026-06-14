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
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IMasterDataService;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RequestSource;
import serp.project.school_bus_service.enums.RequestType;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.RequestStudentEntity;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.entity.StudentEntity;
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
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;

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
    private final ISchoolPickupPointService schoolPickupPointService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusDataScopeService schoolBusDataScopeService;
    private final SchoolBusSecurityService securityService;


    public TransportRequestServiceImpl(
            TransportRequestRepository transportRequestRepository,
            RequestStudentRepository requestStudentRepository,
            TransportRequestHistoryRepository transportRequestHistoryRepository,
            IMasterDataService masterDataService,
            IStudentSubscriptionService subscriptionService,
            ISchoolPickupPointService schoolPickupPointService,
            ICodeGeneratorService codeGeneratorService,
            SchoolBusMapper mapper,
            MessageCommon messageCommon,
            ISchoolBusDataScopeService schoolBusDataScopeService,
            SchoolBusSecurityService securityService) {
        this.transportRequestRepository = transportRequestRepository;
        this.requestStudentRepository = requestStudentRepository;
        this.transportRequestHistoryRepository = transportRequestHistoryRepository;
        this.masterDataService = masterDataService;
        this.subscriptionService = subscriptionService;
        this.schoolPickupPointService = schoolPickupPointService;
        this.codeGeneratorService = codeGeneratorService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusDataScopeService = schoolBusDataScopeService;
        this.securityService = securityService;
    }


    @Override
    protected BaseRepository<TransportRequestEntity, Long> getRepository() {
        return transportRequestRepository;
    }

    @Override
    public PageResponse<TransportRequestResponse> getTransportRequests(TransportRequestParamsRequest params, Long tenantId) {
        // Parent data scope: filter by current parent profile
        Specification<TransportRequestEntity> baseSpec = spec(tenantId, params == null ? null : params.getKeyword(), "parentProfile.fullName", "school.name",
                        "requestType", "status", "notes");
        if (securityService.isParentOnly()) {
            Long parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            baseSpec = baseSpec.and((root, query, cb) -> cb.equal(root.get("parentProfile").get("id"), parentProfileId));
        }

        PageResponse<TransportRequestResponse> response = PageResponse.from(transportRequestRepository.findAll(
                baseSpec,
                pageable(params, Set.of("id", "requestType", "status", "effectiveFrom", "effectiveTo", "createdAt",
                        "updatedAt"), "createdAt")),
                mapper::toTransportRequestResponse);

        List<TransportRequestResponse> items = response.getItems();
        if (items != null && !items.isEmpty()) {
            List<Long> requestIds = items.stream().map(TransportRequestResponse::getId).toList();
            List<Object[]> counts = requestStudentRepository.countStudentsByRequestIds(requestIds, tenantId);
            Map<Long, Integer> countMap = counts.stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> ((Number) row[1]).intValue()
                    ));
            items.forEach(item -> item.setStudentCount(countMap.getOrDefault(item.getId(), 0)));
        }

        return response;
    }

    @Override
    public TransportRequestDetailResponse getTransportRequest(Long id, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessTransportRequest(id);
        TransportRequestEntity request = findById(transportRequestRepository, id, tenantId);
        return mapper.toTransportRequestDetailResponse(request,
                requestStudentRepository.findByRequestIdAndTenantIdAndIsDeletedFalse(id, tenantId));
    }

    @Override
    public List<RequestStudentResponse> getRequestStudents(Long requestId, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessTransportRequest(requestId);
        findById(transportRequestRepository, requestId, tenantId);
        return requestStudentRepository.findByRequestIdAndTenantIdAndIsDeletedFalse(requestId, tenantId).stream()
                .map(mapper::toRequestStudentResponse)
                .toList();
    }

    @Override
    public List<TransportRequestHistoryResponse> getTransportRequestHistory(Long requestId, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessTransportRequest(requestId);
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
        // Parent data scope: override parentProfileId from security context
        if (securityService.isParentOnly()) {
            Long currentParentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            request.setParentProfileId(currentParentProfileId);
        }

        TransportRequestEntity entity = new TransportRequestEntity();
        entity.markCreated(tenantId, actor(actorId));
        applyTransportRequest(entity, request, tenantId);
        entity.setRequestCode(generateCode(SchoolBusCode.REQUEST, tenantId, actorId));
        entity.setRequestedAt(LocalDateTime.now());
        entity.setRequestSource(securityService.isParent() ? RequestSource.PARENT : RequestSource.ADMIN);
        entity.setChangeReason(request.getChangeReason());
        entity.setStatus(RequestStatus.SUBMITTED);
        entity.setApprovedAt(null);
        entity.setApprovedBy(null);
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        replaceRequestStudents(saved, request.getStudents(), tenantId, actorId);
        recordHistory(saved, null, RequestStatus.SUBMITTED, actorId, null, "Created transport request");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse updateTransportRequest(Long id, TransportRequestUpsertRequest request, Long tenantId,
            Long actorId) {
        schoolBusDataScopeService.assertCanAccessTransportRequest(id);
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() == RequestStatus.APPROVED || entity.getStatus() == RequestStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Request.CANNOT_EDIT, messageCommon.getMessage(AppErrorCode.Request.CANNOT_EDIT, entity.getStatus()));
        }
        // Parent data scope: override parentProfileId from security context
        if (securityService.isParentOnly()) {
            Long currentParentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            request.setParentProfileId(currentParentProfileId);
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
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse approveTransportRequest(Long id, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanAccessTransportRequest(id);
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() != RequestStatus.SUBMITTED) {
            throw new AppException(AppErrorCode.Request.ONLY_SUBMITTED_APPROVED, messageCommon.getMessage(AppErrorCode.Request.ONLY_SUBMITTED_APPROVED));
        }

        List<RequestStudentEntity> requestStudents = requestStudentRepository
                .findByRequestIdAndTenantIdAndIsDeletedFalse(entity.getId(), tenantId);

        boolean requiresRoutingValidation = entity.getRequestType() == RequestType.NEW_SERVICE 
                || entity.getRequestType() == RequestType.CHANGE_SERVICE 
                || entity.getRequestType() == RequestType.RENEW_SERVICE;

        if (requiresRoutingValidation) {
            for (RequestStudentEntity rs : requestStudents) {
                TripOption opt = rs.getTripOption();
                if (opt == null) {
                    throw new AppException(AppErrorCode.Request.INVALID_STATE,
                            "Missing trip option for student #" + rs.getStudent().getId()
                                    + ". Please edit the request and select a trip option before approving.");
                }

                boolean needsPickup = opt == TripOption.MORNING || opt == TripOption.ROUND_TRIP;
                boolean needsDropoff = opt == TripOption.AFTERNOON || opt == TripOption.ROUND_TRIP;

                if (needsPickup) {
                    PickupPointEntity pickup = rs.getPickupPoint();
                    if (pickup == null) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Missing pickup point for student #" + rs.getStudent().getId()
                                        + " (trip option: " + opt + ")");
                    }
                    if (pickup.getLatitude() == null || pickup.getLongitude() == null) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Pickup point '" + pickup.getName() + "' is missing coordinates for student #"
                                        + rs.getStudent().getId() + ". Configure coordinates on the pickup point before approving.");
                    }
                    schoolPickupPointService.findLinkBySchoolAndPickupPoint(entity.getSchool().getId(), pickup.getId(), tenantId)
                            .orElseThrow(() -> new AppException(AppErrorCode.Request.INVALID_STATE,
                                    "Pickup point '" + pickup.getName() + "' is not linked to school '" + entity.getSchool().getName() + "'"));
                }

                if (needsDropoff) {
                    PickupPointEntity dropoff = rs.getDropoffPoint();
                    if (dropoff == null) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Missing drop-off point for student #" + rs.getStudent().getId()
                                        + " (trip option: " + opt + ")");
                    }
                    if (dropoff.getLatitude() == null || dropoff.getLongitude() == null) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Drop-off point '" + dropoff.getName() + "' is missing coordinates for student #"
                                        + rs.getStudent().getId() + ". Configure coordinates on the drop-off point before approving.");
                    }
                    schoolPickupPointService.findLinkBySchoolAndPickupPoint(entity.getSchool().getId(), dropoff.getId(), tenantId)
                            .orElseThrow(() -> new AppException(AppErrorCode.Request.INVALID_STATE,
                                    "Drop-off point '" + dropoff.getName() + "' is not linked to school '" + entity.getSchool().getName() + "'"));
                }
            }
        }

        entity.markUpdated(actor(actorId));
        RequestStatus oldStatus = entity.getStatus();
        entity.setStatus(RequestStatus.APPROVED);
        entity.setApprovedBy(actorId);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);

        for (RequestStudentEntity requestStudent : requestStudents) {
            dispatchApprove(saved, requestStudent, tenantId, actorId);
        }

        recordHistory(saved, oldStatus, RequestStatus.APPROVED, actorId, null,
                "Approved transport request (" + saved.getRequestType().name() + ")");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse rejectTransportRequest(Long id, RejectRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanAccessTransportRequest(id);
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
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse cancelTransportRequest(Long id, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanAccessTransportRequest(id);
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
        return toResponse(saved);
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

            RequestStudentEntity rs = new RequestStudentEntity();
            rs.markCreated(tenantId, actor(actorId));
            rs.setRequest(entity);

            StudentEntity student = masterDataService.getStudent(studentRequest.getStudentId(), tenantId);
            if (student == null || Boolean.TRUE.equals(student.getIsDeleted()) || Boolean.FALSE.equals(student.getIsActive())) {
                throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                        "Student #" + studentRequest.getStudentId() + " is inactive or deleted");
            }
            if (student.getParentProfile() == null || !student.getParentProfile().getId().equals(entity.getParentProfile().getId())) {
                throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                        "Student #" + student.getId() + " does not belong to the selected parent profile");
            }
            if (student.getSchool() == null || !student.getSchool().getId().equals(schoolId)) {
                throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                        "Student #" + student.getId() + " does not belong to the selected school");
            }
            rs.setStudent(student);

            // ── Routing requirement check: NEW/CHANGE/RENEW must supply tripOption ──
            boolean requiresRouting = requestType == RequestType.NEW_SERVICE
                    || requestType == RequestType.CHANGE_SERVICE
                    || requestType == RequestType.RENEW_SERVICE;

            if (requiresRouting && (studentRequest.getTripOption() == null || studentRequest.getTripOption().isBlank())) {
                throw new AppException(AppErrorCode.Request.INVALID_REQUEST,
                        "Trip option is required for request type " + requestType
                                + " (student #" + studentRequest.getStudentId() + ")");
            }

            // ── Days of week ───────────────────────────────────────────────
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
                
                SubscriptionStatus ts = target.getStatus();
                if (requestType == RequestType.PAUSE_SERVICE) {
                    if (ts != SubscriptionStatus.ACTIVE) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Cannot pause subscription with status " + ts + ". Only ACTIVE subscriptions can be paused.");
                    }
                    if (subscriptionService.hasOverlappingPausePeriod(target.getId(), requestFrom, requestTo, tenantId)) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Cannot pause subscription: there is already an overlapping active/scheduled pause period.");
                    }
                } else if (requestType == RequestType.RESUME_SERVICE) {
                    if (ts == SubscriptionStatus.PAUSED) {
                        // OK
                    } else if (ts == SubscriptionStatus.ACTIVE) {
                        boolean hasPauses = subscriptionService.hasActiveOrScheduledPause(target.getId(), tenantId);
                        if (!hasPauses) {
                            throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                    "Cannot resume subscription: subscription is already ACTIVE and has no active or scheduled pause periods.");
                        }
                    } else {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Cannot resume subscription with status " + ts + ". Only PAUSED or ACTIVE (with pause periods) subscriptions can be resumed.");
                    }
                } else if (requestType == RequestType.STOP_SERVICE) {
                    if (ts == SubscriptionStatus.STOPPED || ts == SubscriptionStatus.EXPIRED) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Cannot stop subscription: subscription is already " + ts);
                    }
                } else if (requestType == RequestType.CHANGE_SERVICE) {
                    if (ts == SubscriptionStatus.STOPPED || ts == SubscriptionStatus.EXPIRED) {
                        throw new AppException(AppErrorCode.Request.INVALID_STATE,
                                "Cannot apply CHANGE_SERVICE to a " + ts + " subscription.");
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

    private TransportRequestResponse toResponse(TransportRequestEntity entity) {
        TransportRequestResponse response = mapper.toTransportRequestResponse(entity);
        if (response != null) {
            response.setStudentCount(requestStudentRepository.findByRequestIdAndTenantIdAndIsDeletedFalse(entity.getId(), entity.getTenantId()).size());
        }
        return response;
    }
}
