package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.StudentSubscriptionParamsRequest;
import serp.project.school_bus_service.application.dto.request.StudentSubscriptionUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.StudentSubscriptionResponse;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentSubscriptionEntity;
import serp.project.school_bus_service.infrastructure.store.model.TransportRequestEntity;

import java.time.LocalDate;
import java.util.List;

public interface IStudentSubscriptionService {

    PageResponse<StudentSubscriptionResponse> getSubscriptions(StudentSubscriptionParamsRequest params, Long tenantId);

    StudentSubscriptionResponse getSubscription(Long id, Long tenantId);

    StudentSubscriptionEntity getSubscriptionEntity(Long id, Long tenantId);

    StudentSubscriptionResponse createSubscription(StudentSubscriptionUpsertRequest request, Long tenantId,
            Long actorId);

    StudentSubscriptionResponse updateSubscription(Long id, StudentSubscriptionUpsertRequest request, Long tenantId,
            Long actorId);

    StudentSubscriptionResponse activateSubscription(Long id, Long tenantId, Long actorId);

    StudentSubscriptionResponse pauseSubscription(Long id, Long tenantId, Long actorId);

    StudentSubscriptionResponse stopSubscription(Long id, Long tenantId, Long actorId);

    List<StudentSubscriptionEntity> findEligibleSubscriptions(Long schoolId, RouteDirection direction,
            LocalDate serviceDate, Long tenantId);

    StudentSubscriptionEntity createFromApprovedRequest(
            TransportRequestEntity request,
            RequestStudentEntity requestStudent,
            TripOption tripOption,
            Long tenantId,
            Long actorId);
}
