package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.StudentSubscriptionParamsRequest;
import serp.project.school_bus_service.dto.request.StudentSubscriptionUpsertRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.StudentSubscriptionHistoryResponse;
import serp.project.school_bus_service.dto.response.StudentSubscriptionResponse;

import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.entity.RequestStudentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.TransportRequestEntity;

import java.time.LocalDate;
import java.util.List;

public interface IStudentSubscriptionService extends IBaseService<StudentSubscriptionEntity, Long> {

    PageResponse<StudentSubscriptionResponse> getSubscriptions(StudentSubscriptionParamsRequest params, Long tenantId);

    StudentSubscriptionResponse getSubscription(Long id, Long tenantId);

    List<StudentSubscriptionEntity> findAllBySchoolIdAndTenantId(Long schoolId, Long tenantId);

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
            Long tenantId, Long actorId);

    void changeFromApprovedRequest(
            TransportRequestEntity request,
            RequestStudentEntity requestStudent,
            Long tenantId, Long actorId);

    void stopFromApprovedRequest(
            TransportRequestEntity request,
            RequestStudentEntity requestStudent,
            Long tenantId, Long actorId);

    void pauseFromApprovedRequest(
            TransportRequestEntity request,
            RequestStudentEntity requestStudent,
            Long tenantId, Long actorId);

    void resumeFromApprovedRequest(
            TransportRequestEntity request,
            RequestStudentEntity requestStudent,
            Long tenantId, Long actorId);

    StudentSubscriptionEntity renewFromApprovedRequest(
            TransportRequestEntity request,
            RequestStudentEntity requestStudent,
            Long tenantId, Long actorId);

    List<StudentSubscriptionHistoryResponse> getSubscriptionHistory(Long subscriptionId, Long tenantId);

    /** Find eligible subscriptions for route planning (DB-filtered). */
    List<StudentSubscriptionEntity> findEligibleForPlanning(
            Long schoolId, Long tenantId, LocalDate serviceDate,
            int dayIndex, List<TripOption> allowedTripOptions,
            boolean isOutbound);

    /** Batch check: which subscription IDs are paused on the given date? */
    List<Long> findPausedSubscriptionIds(List<Long> subscriptionIds, Long tenantId, LocalDate serviceDate);

    boolean hasOverlappingPausePeriod(Long subscriptionId, LocalDate pauseFrom, LocalDate pauseTo, Long tenantId);

    boolean hasActiveOrScheduledPause(Long subscriptionId, Long tenantId);
}
