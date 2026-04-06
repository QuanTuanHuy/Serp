package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.request.RejectRequest;
import serp.project.school_bus_service.application.dto.request.RequestStudentItemRequest;
import serp.project.school_bus_service.application.dto.request.TransportRequestUpsertRequest;
import serp.project.school_bus_service.application.dto.response.RequestStudentResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestDetailResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.core.service.ITransportRequestService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RequestType;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.TransportRequestEntity;
import serp.project.school_bus_service.infrastructure.store.repository.RequestStudentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TransportRequestRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportRequestServiceImpl extends AbstractBaseService<TransportRequestEntity, Long>
        implements ITransportRequestService {

    private final TransportRequestRepository transportRequestRepository;
    private final RequestStudentRepository requestStudentRepository;
    private final IMasterDataService masterDataService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<TransportRequestEntity, Long> getRepository() {
        return transportRequestRepository;
    }

    @Override
    public List<TransportRequestResponse> getTransportRequests(Long tenantId) {
        return transportRequestRepository.findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(tenantId).stream()
                .map(mapper::toTransportRequestResponse)
                .toList();
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
    @Transactional
    public TransportRequestResponse createTransportRequest(TransportRequestUpsertRequest request, Long tenantId, Long actorId) {
        TransportRequestEntity entity = new TransportRequestEntity();
        entity.markCreated(tenantId, actor(actorId));
        applyTransportRequest(entity, request, tenantId);
        entity.setStatus(RequestStatus.PENDING);
        entity.setApprovedAt(null);
        entity.setApprovedBy(null);
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        replaceRequestStudents(saved, request.getStudents(), tenantId, actorId);
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
        applyTransportRequest(entity, request, tenantId);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
        replaceRequestStudents(saved, request.getStudents(), tenantId, actorId);
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
        entity.setStatus(RequestStatus.APPROVED);
        entity.setApprovedBy(actorId);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setRejectionReason(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
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
        entity.setStatus(RequestStatus.REJECTED);
        entity.setRejectionReason(request.getReason());
        entity.setApprovedBy(null);
        entity.setApprovedAt(null);
        TransportRequestEntity saved = transportRequestRepository.save(entity);
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
}
