package serp.project.school_bus_service.service;
import serp.project.school_bus_service.entity.TransportRequestEntity;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.TransportRequestParamsRequest;
import serp.project.school_bus_service.dto.request.RejectRequest;
import serp.project.school_bus_service.dto.request.TransportRequestUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.RequestStudentResponse;
import serp.project.school_bus_service.dto.response.TransportRequestDetailResponse;
import serp.project.school_bus_service.dto.response.TransportRequestResponse;
import serp.project.school_bus_service.dto.response.TransportRequestSummaryResponse;
import serp.project.school_bus_service.entity.RequestStudentEntity;
import serp.project.school_bus_service.enums.RequestStatus;

import java.time.LocalDate;
import java.util.List;

public interface ITransportRequestService extends IBaseService<TransportRequestEntity, Long> {

    PageResponse<TransportRequestResponse> getTransportRequests(TransportRequestParamsRequest params, Long tenantId);

    TransportRequestSummaryResponse getSummary(Long tenantId);

    TransportRequestDetailResponse getTransportRequest(Long id, Long tenantId);

    List<RequestStudentResponse> getRequestStudents(Long requestId, Long tenantId);

    TransportRequestResponse createTransportRequest(TransportRequestUpsertRequest request, Long tenantId, Long actorId);

    TransportRequestResponse updateTransportRequest(Long id, TransportRequestUpsertRequest request, Long tenantId, Long actorId);

    TransportRequestResponse approveTransportRequest(Long id, Long tenantId, Long actorId);

    TransportRequestResponse rejectTransportRequest(Long id, RejectRequest request, Long tenantId, Long actorId);

    TransportRequestResponse cancelTransportRequest(Long id, Long tenantId, Long actorId);

    boolean hasApprovedRequestForStudent(Long studentId, Long schoolId, LocalDate serviceDate, Long tenantId);

    /** Internal: returns approved RequestStudent entities for manifest building. */
    List<RequestStudentEntity> findApprovedManifestStudents(Long schoolId, LocalDate serviceDate, Long tenantId);

    long countByTenant(Long tenantId);

    long countByTenantAndStatus(Long tenantId, RequestStatus status);

    long countBySchoolAndTenant(Long schoolId, Long tenantId);
}
