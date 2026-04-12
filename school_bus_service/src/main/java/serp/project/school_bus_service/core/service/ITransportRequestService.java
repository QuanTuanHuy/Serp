package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.TransportRequestParamsRequest;
import serp.project.school_bus_service.application.dto.request.RejectRequest;
import serp.project.school_bus_service.application.dto.request.TransportRequestUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.RequestStudentResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestDetailResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestResponse;

import java.time.LocalDate;
import java.util.List;

public interface ITransportRequestService {

    PageResponse<TransportRequestResponse> getTransportRequests(TransportRequestParamsRequest params, Long tenantId);

    TransportRequestDetailResponse getTransportRequest(Long id, Long tenantId);

    List<RequestStudentResponse> getRequestStudents(Long requestId, Long tenantId);

    TransportRequestResponse createTransportRequest(TransportRequestUpsertRequest request, Long tenantId, Long actorId);

    TransportRequestResponse updateTransportRequest(Long id, TransportRequestUpsertRequest request, Long tenantId, Long actorId);

    TransportRequestResponse approveTransportRequest(Long id, Long tenantId, Long actorId);

    TransportRequestResponse rejectTransportRequest(Long id, RejectRequest request, Long tenantId, Long actorId);

    boolean hasApprovedRequestForStudent(Long studentId, Long schoolId, LocalDate serviceDate, Long tenantId);
}
