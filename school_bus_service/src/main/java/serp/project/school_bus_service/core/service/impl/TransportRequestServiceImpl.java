package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.TransportRequestParamsRequest;
import serp.project.school_bus_service.application.dto.request.RejectRequest;
import serp.project.school_bus_service.application.dto.request.RequestStudentItemRequest;
import serp.project.school_bus_service.application.dto.request.TransportRequestUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.RequestStudentResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestDetailResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestHistoryResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.core.service.IStudentSubscriptionService;
import serp.project.school_bus_service.core.service.ITransportRequestService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RequestSource;
import serp.project.school_bus_service.enums.RequestType;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.TransportRequestHistoryEntity;
import serp.project.school_bus_service.infrastructure.store.model.TransportRequestEntity;
import serp.project.school_bus_service.infrastructure.store.repository.RequestStudentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TransportRequestHistoryRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TransportRequestRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransportRequestServiceImpl extends AbstractBaseService<TransportRequestEntity, Long>
        implements ITransportRequestService {

    private final TransportRequestRepository transportRequestRepository;
    private final RequestStudentRepository requestStudentRepository;
    private final TransportRequestHistoryRepository transportRequestHistoryRepository;
    private final IMasterDataService masterDataService;
    private final IStudentSubscriptionService subscriptionService;
    private final ICodeGeneratorService codeGeneratorService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;

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
        entity.setStatus(RequestStatus.PENDING);
        entity.setApprovedAt(null);
        entity.setApprovedBy(null);
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        replaceRequestStudents(saved, request.getStudents(), tenantId, actorId);
        recordHistory(saved, null, RequestStatus.PENDING, actorId, null, "Created transport request");
        auditLogService.log(tenantId, actorId, "TransportRequest", saved.getId(), "CREATE", "Created transport request");
        return mapper.toTransportRequestResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse updateTransportRequest(Long id, TransportRequestUpsertRequest request, Long tenantId,
            Long actorId) {
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() == RequestStatus.APPROVED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }
        entity.markUpdated(actor(actorId));
        RequestStatus oldStatus = entity.getStatus();
        applyTransportRequest(entity, request, tenantId);
        entity.setChangeReason(request.getChangeReason());
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
        if (entity.getStatus() != RequestStatus.PENDING) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        entity.markUpdated(actor(actorId));
        RequestStatus oldStatus = entity.getStatus();
        entity.setStatus(RequestStatus.APPROVED);
        entity.setApprovedBy(actorId);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        for (RequestStudentEntity requestStudent : requestStudentRepository
                .findByRequestIdAndTenantIdAndIsDeletedFalse(saved.getId(), tenantId)) {
            subscriptionService.createFromApprovedRequest(saved, requestStudent, tripOption(saved.getRequestType()),
                    tenantId, actorId);
        }
        recordHistory(saved, oldStatus, RequestStatus.APPROVED, actorId, null,
                "Approved transport request and created subscriptions");
        auditLogService.log(tenantId, actorId, "TransportRequest", saved.getId(), "APPROVE", "Approved transport request");
        return mapper.toTransportRequestResponse(saved);
    }

    @Override
    @Transactional
    public TransportRequestResponse rejectTransportRequest(Long id, RejectRequest request, Long tenantId, Long actorId) {
        TransportRequestEntity entity = findById(transportRequestRepository, id, tenantId);
        if (entity.getStatus() != RequestStatus.PENDING) {
            throw new AppException(AppErrorCode.INVALID_STATE);
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
    public boolean hasApprovedRequestForStudent(Long studentId, Long schoolId, LocalDate serviceDate, Long tenantId) {
        return requestStudentRepository.findByStudentIdAndTenantIdAndIsDeletedFalse(studentId, tenantId).stream()
                .map(RequestStudentEntity::getRequest)
                .filter(request -> request.getSchool().getId().equals(schoolId))
                .filter(request -> request.getStatus() == RequestStatus.APPROVED)
                .anyMatch(request -> !serviceDate.isBefore(request.getEffectiveFrom())
                        && (request.getEffectiveTo() == null || !serviceDate.isAfter(request.getEffectiveTo())));
    }

    private void applyTransportRequest(TransportRequestEntity entity, TransportRequestUpsertRequest request, Long tenantId) {
        entity.setParentProfile(masterDataService.getParent(request.getParentProfileId(), tenantId));
        entity.setSchool(masterDataService.getSchool(request.getSchoolId(), tenantId));
        entity.setRequestType(RequestType.valueOf(request.getRequestType().toUpperCase()));
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setNotes(request.getNotes());
        entity.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void replaceRequestStudents(TransportRequestEntity entity, List<RequestStudentItemRequest> students, Long tenantId,
            Long actorId) {
        requestStudentRepository.softDeleteByRequestId(entity.getId(), tenantId, actor(actorId));
        for (RequestStudentItemRequest studentRequest : students) {
            RequestStudentEntity requestStudent = new RequestStudentEntity();
            requestStudent.markCreated(tenantId, actor(actorId));
            requestStudent.setRequest(entity);
            requestStudent.setStudent(masterDataService.getStudent(studentRequest.getStudentId(), tenantId));
            requestStudent.setPickupPoint(studentRequest.getPickupPointId() == null
                    ? null
                    : masterDataService.getPickupPoint(studentRequest.getPickupPointId(), tenantId));
            requestStudentRepository.save(requestStudent);
        }
    }

    private Specification<TransportRequestEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            serp.project.school_bus_service.application.dto.request.BaseParamsRequest params,
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

    private TripOption tripOption(RequestType requestType) {
        return switch (requestType) {
            case PICKUP -> TripOption.MORNING;
            case DROPOFF -> TripOption.AFTERNOON;
            case ROUND_TRIP -> TripOption.ROUND_TRIP;
        };
    }

    private String generateCode(SchoolBusCode code, Long tenantId, Long actorId) {
        return codeGeneratorService.generate(code.sequenceKey(), code.prefix(), tenantId, actorId);
    }
}
