package serp.project.school_bus_service.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.request.NotificationSendCommand;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.AttendanceEventType;
import serp.project.school_bus_service.enums.NotificationCategory;
import serp.project.school_bus_service.enums.NotificationPriority;
import serp.project.school_bus_service.enums.NotificationType;
import serp.project.school_bus_service.enums.RouteAssignmentStatus;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.service.ISchoolBusDomainNotificationService;
import serp.project.school_bus_service.service.ISchoolBusNotificationService;
import serp.project.school_bus_service.service.ISchoolBusNotificationRecipientService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class SchoolBusDomainNotificationService implements ISchoolBusDomainNotificationService {

    private final ISchoolBusNotificationService notificationService;
    private final ISchoolBusNotificationRecipientService recipientService;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private static final List<RouteAssignmentStatus> CURRENT_ASSIGNMENT_STATUSES =
            List.of(RouteAssignmentStatus.ASSIGNED, RouteAssignmentStatus.CONFIRMED);

    public SchoolBusDomainNotificationService(
            ISchoolBusNotificationService notificationService,
            ISchoolBusNotificationRecipientService recipientService,
            RouteAssignmentRepository routeAssignmentRepository) {
        this.notificationService = notificationService;
        this.recipientService = recipientService;
        this.routeAssignmentRepository = routeAssignmentRepository;
    }

    @Override
    public void notifyTransportRequestSubmitted(TransportRequestEntity request, Long actorId) {
        safely("REQUEST_SUBMITTED", request, () -> send(
                request.getTenantId(),
                recipientService.findOperatorAccountUserIds(request.getTenantId()),
                actorId,
                "REQUEST_SUBMITTED",
                "TRANSPORT_REQUEST",
                request.getId(),
                "Có yêu cầu xe bus mới",
                "Yêu cầu xe buýt " + request.getRequestCode() + " đang chờ xem xét.",
                NotificationType.INFO,
                NotificationCategory.SCHOOL_BUS_REQUEST,
                NotificationPriority.MEDIUM,
                "/school-bus/requests/" + request.getId(),
                metadata("requestCode", request.getRequestCode())));
    }

    @Override
    public void notifyTransportRequestApproved(TransportRequestEntity request, Long actorId) {
        safely("REQUEST_APPROVED", request, () -> send(
                request.getTenantId(),
                recipientService.findParentAccountUserIds(request.getParentProfile()),
                actorId,
                "REQUEST_APPROVED",
                "TRANSPORT_REQUEST",
                request.getId(),
                "Yêu cầu xe buýt đã được phê duyệt",
                "Yêu cầu xe buýt " + request.getRequestCode() + " của bạn đã được phê duyệt.",
                NotificationType.SUCCESS,
                NotificationCategory.SCHOOL_BUS_REQUEST,
                NotificationPriority.MEDIUM,
                "/school-bus/requests/" + request.getId(),
                metadata("requestCode", request.getRequestCode())));
    }

    @Override
    public void notifyTransportRequestRejected(TransportRequestEntity request, Long actorId) {
        safely("REQUEST_REJECTED", request, () -> send(
                request.getTenantId(),
                recipientService.findParentAccountUserIds(request.getParentProfile()),
                actorId,
                "REQUEST_REJECTED",
                "TRANSPORT_REQUEST",
                request.getId(),
                "Yêu cầu xe buýt đã bị từ chối",
                appendReason(
                        "Yêu cầu xe buýt " + request.getRequestCode() + " của bạn đã bị từ chối.",
                        request.getRejectionReason()),
                NotificationType.WARNING,
                NotificationCategory.SCHOOL_BUS_REQUEST,
                NotificationPriority.HIGH,
                "/school-bus/requests/" + request.getId(),
                metadata("requestCode", request.getRequestCode())));
    }

    @Override
    public void notifyTransportRequestCancelled(TransportRequestEntity request, Long actorId) {
        safely("REQUEST_CANCELLED", request, () -> {
            Set<Long> recipients = recipientService.findOperatorAccountUserIds(request.getTenantId());
            recipients.addAll(recipientService.findParentAccountUserIds(request.getParentProfile()));
            send(
                    request.getTenantId(),
                    recipients,
                    actorId,
                    "REQUEST_CANCELLED",
                    "TRANSPORT_REQUEST",
                    request.getId(),
                    "Yêu cầu xe buýt đã bị hủy",
                    "Yêu cầu xe buýt " + request.getRequestCode() + " đã bị hủy.",
                    NotificationType.WARNING,
                    NotificationCategory.SCHOOL_BUS_REQUEST,
                    NotificationPriority.HIGH,
                    "/school-bus/requests/" + request.getId(),
                    metadata("requestCode", request.getRequestCode()));
        });
    }

    @Override
    public void notifySubscriptionCreated(StudentSubscriptionEntity subscription, Long actorId) {
        notifySubscription(
                subscription,
                actorId,
                "SUBSCRIPTION_CREATED",
                "Đăng ký dịch vụ xe bus đã được tạo",
                "Đăng ký dịch vụ xe bus của " + studentName(subscription) + " đã được tạo.",
                NotificationType.SUCCESS,
                NotificationPriority.MEDIUM);
    }

    @Override
    public void notifySubscriptionUpdated(StudentSubscriptionEntity subscription, Long actorId) {
        notifySubscription(
                subscription,
                actorId,
                "SUBSCRIPTION_UPDATED",
                "Đăng ký dịch vụ xe bus đã được cập nhật",
                "Đăng ký dịch vụ xe bus của " + studentName(subscription) + " đã được cập nhật.",
                NotificationType.SUCCESS,
                NotificationPriority.MEDIUM);
    }

    @Override
    public void notifySubscriptionStatusChanged(
            StudentSubscriptionEntity subscription,
            SubscriptionStatus status,
            Long actorId) {
        NotificationType type = status == SubscriptionStatus.ACTIVE
                ? NotificationType.SUCCESS
                : NotificationType.WARNING;
        NotificationPriority priority = status == SubscriptionStatus.STOPPED
                ? NotificationPriority.HIGH
                : NotificationPriority.MEDIUM;
        notifySubscription(
                subscription,
                actorId,
                "SUBSCRIPTION_" + status.name(),
                "Trạng thái đăng ký dịch vụ xe bus đã thay đổi",
                "Đăng ký dịch vụ xe bus của " + studentName(subscription)
                        + " hiện là " + subscriptionStatusLabel(status) + ".",
                type,
                priority);
    }

    @Override
    public void notifyRouteAssigned(RouteAssignmentEntity assignment, Long actorId) {
        safely("ROUTE_ASSIGNED", assignment, () -> {
            Set<Long> recipients = new LinkedHashSet<>();
            recipients.addAll(recipientService.findDriverAccountUserIds(assignment.getDriver()));
            recipients.addAll(recipientService.findAttendantAccountUserIds(assignment.getAttendant()));
            send(
                    assignment.getTenantId(),
                    recipients,
                    actorId,
                    "ROUTE_ASSIGNED",
                    "ROUTE",
                    assignment.getRoute().getId(),
                    "Bạn được phân công tuyến xe bus",
                    "Bạn được phân công tuyến " + assignment.getRoute().getRouteCode() + ".",
                    NotificationType.INFO,
                    NotificationCategory.SCHOOL_BUS_DISPATCH,
                    NotificationPriority.HIGH,
                    "/school-bus/trips",
                    metadata("routeCode", assignment.getRoute().getRouteCode()));
        });
    }

    @Override
    public void notifyTripCreated(TripExecutionEntity trip, Long actorId) {
        safely("TRIP_CREATED", trip, () -> {
            Set<Long> recipients = recipientService.findTripParentAccountUserIds(trip);
            RouteAssignmentEntity assignment = currentAssignment(trip);
            recipients.addAll(recipientService.findDriverAccountUserIds(
                    assignment == null ? null : assignment.getDriver()));
            recipients.addAll(recipientService.findAttendantAccountUserIds(
                    assignment == null ? null : assignment.getAttendant()));
            sendTrip(
                    trip,
                    recipients,
                    actorId,
                    "TRIP_CREATED",
                    "Chuyến xe bus đã được lên lịch",
                    "Chuyến " + trip.getTripCode() + " đã được lên lịch.",
                    NotificationType.INFO,
                    NotificationPriority.MEDIUM);
        });
    }

    @Override
    public void notifyTripStarted(TripExecutionEntity trip, Long actorId) {
        safely("TRIP_STARTED", trip, () -> sendTrip(
                trip,
                recipientService.findTripParentAccountUserIds(trip),
                actorId,
                "TRIP_STARTED",
                "Chuyến xe bus đã bắt đầu",
                "Chuyến " + trip.getTripCode() + " đã bắt đầu.",
                NotificationType.INFO,
                NotificationPriority.HIGH));
    }

    @Override
    public void notifyTripCompleted(TripExecutionEntity trip, Long actorId) {
        safely("TRIP_COMPLETED", trip, () -> sendTrip(
                trip,
                recipientService.findTripParentAccountUserIds(trip),
                actorId,
                "TRIP_COMPLETED",
                "Chuyến xe bus đã hoàn thành",
                "Chuyến " + trip.getTripCode() + " đã hoàn thành.",
                NotificationType.SUCCESS,
                NotificationPriority.MEDIUM));
    }

    @Override
    public void notifyTripCancelled(TripExecutionEntity trip, Long actorId) {
        safely("TRIP_CANCELLED", trip, () -> {
            Set<Long> recipients = recipientService.findTripParentAccountUserIds(trip);
            RouteAssignmentEntity assignment = currentAssignment(trip);
            recipients.addAll(recipientService.findDriverAccountUserIds(
                    assignment == null ? null : assignment.getDriver()));
            recipients.addAll(recipientService.findAttendantAccountUserIds(
                    assignment == null ? null : assignment.getAttendant()));
            recipients.addAll(recipientService.findOperatorAccountUserIds(trip.getTenantId()));
            sendTrip(
                    trip,
                    recipients,
                    actorId,
                    "TRIP_CANCELLED",
                    "Chuyến xe bus đã bị hủy",
                    appendReason(
                            "Chuyến " + trip.getTripCode() + " đã bị hủy.",
                            trip.getCancellationReason()),
                    NotificationType.ERROR,
                    NotificationPriority.HIGH);
        });
    }

    @Override
    public void notifyAttendanceRecorded(
            TripExecutionEntity trip,
            TripStudentEntity tripStudent,
            AttendanceEventType eventType,
            Long actorId) {
        safely("ATTENDANCE_" + eventType.name(), trip, () -> {
            boolean successful = eventType == AttendanceEventType.BOARDED
                    || eventType == AttendanceEventType.DROPPED_OFF;
            String eventLabel = attendanceLabel(eventType);
            send(
                    trip.getTenantId(),
                    recipientService.findParentAccountUserIds(tripStudent.getStudent().getParentProfile()),
                    actorId,
                    "ATTENDANCE_" + eventType.name(),
                    "TRIP_ATTENDANCE",
                    tripStudent.getId(),
                    studentName(tripStudent) + " " + eventLabel,
                    studentName(tripStudent) + " " + eventLabel + " trong chuyến " + trip.getTripCode() + ".",
                    successful ? NotificationType.SUCCESS : NotificationType.WARNING,
                    NotificationCategory.SCHOOL_BUS_ATTENDANCE,
                    successful ? NotificationPriority.MEDIUM : NotificationPriority.HIGH,
                    "/school-bus/trips/" + trip.getId(),
                    attendanceMetadata(trip, tripStudent, eventType));
        });
    }

    @Override
    public void notifyStopSkipped(
            TripExecutionEntity trip,
            List<TripStudentEntity> affectedStudents,
            String reason,
            Long actorId) {
        safely("STOP_SKIPPED", trip, () -> {
            if (affectedStudents == null || affectedStudents.isEmpty()) {
                return;
            }
            Set<Long> recipients = recipientService.findParentAccountUserIds(affectedStudents);
            send(
                    trip.getTenantId(),
                    recipients,
                    actorId,
                    "STOP_SKIPPED",
                    "TRIP",
                    trip.getId(),
                    "Điểm dừng xe bus đã bị bỏ qua",
                    appendReason(
                            "Một điểm dừng của chuyến " + trip.getTripCode() + " đã bị bỏ qua.",
                            reason),
                    NotificationType.WARNING,
                    NotificationCategory.SCHOOL_BUS_TRIP,
                    NotificationPriority.HIGH,
                    "/school-bus/trips/" + trip.getId(),
                    metadata("tripCode", trip.getTripCode()));
        });
    }

    private void notifySubscription(
            StudentSubscriptionEntity subscription,
            Long actorId,
            String eventType,
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority) {
        safely(eventType, subscription, () -> send(
                subscription.getTenantId(),
                recipientService.findParentAccountUserIds(subscription.getStudent().getParentProfile()),
                actorId,
                eventType,
                "STUDENT_SUBSCRIPTION",
                subscription.getId(),
                title,
                message,
                type,
                NotificationCategory.SCHOOL_BUS_SUBSCRIPTION,
                priority,
                "/school-bus/subscriptions/" + subscription.getId(),
                metadata("subscriptionCode", subscription.getSubscriptionCode())));
    }

    private void sendTrip(
            TripExecutionEntity trip,
            Collection<Long> recipients,
            Long actorId,
            String eventType,
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority) {
        send(
                trip.getTenantId(),
                recipients,
                actorId,
                eventType,
                "TRIP",
                trip.getId(),
                title,
                message,
                type,
                NotificationCategory.SCHOOL_BUS_TRIP,
                priority,
                "/school-bus/trips/" + trip.getId(),
                metadata("tripCode", trip.getTripCode()));
    }

    private void send(
            Long tenantId,
            Collection<Long> recipientIds,
            Long actorId,
            String eventType,
            String entityType,
            Long entityId,
            String title,
            String message,
            NotificationType type,
            NotificationCategory category,
            NotificationPriority priority,
            String actionUrl,
            Map<String, Object> metadata) {
        Set<Long> recipients = new LinkedHashSet<>();
        if (recipientIds != null) {
            recipientIds.stream()
                    .filter(this::isPositive)
                    .filter(userId -> actorId == null || !actorId.equals(userId))
                    .forEach(recipients::add);
        }

        for (Long recipientId : recipients) {
            try {
                NotificationSendCommand command = new NotificationSendCommand();
                command.setTenantId(tenantId);
                command.setUserId(recipientId);
                command.setTitle(title);
                command.setMessage(message);
                command.setType(type);
                command.setCategory(category);
                command.setPriority(priority);
                command.setActionUrl(actionUrl);
                command.setActionType("VIEW_" + entityType);
                command.setEntityType(entityType);
                command.setEntityId(entityId);
                command.setSourceEventId("school-bus:" + eventType + ":" + entityId + ":" + recipientId);
                command.setMetadata(new LinkedHashMap<>(metadata));
                notificationService.sendNotification(command);
            } catch (Exception exception) {
                log.error(
                        "School Bus notification failed: eventType={}, tenantId={}, entityType={}, entityId={}, recipientId={}",
                        eventType,
                        tenantId,
                        entityType,
                        entityId,
                        recipientId,
                        exception);
            }
        }
    }

    private boolean isPositive(Long value) {
        return value != null && value > 0;
    }

    private Map<String, Object> attendanceMetadata(
            TripExecutionEntity trip,
            TripStudentEntity tripStudent,
            AttendanceEventType eventType) {
        Map<String, Object> metadata = metadata("tripCode", trip.getTripCode());
        metadata.put("studentId", tripStudent.getStudent().getId());
        metadata.put("studentName", tripStudent.getStudent().getFullName());
        metadata.put("attendanceEvent", eventType.name());
        return metadata;
    }

    private Map<String, Object> metadata(String key, Object value) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (value != null) {
            metadata.put(key, value);
        }
        return metadata;
    }

    private String studentName(StudentSubscriptionEntity subscription) {
        return subscription.getStudent() == null ? "học sinh" : subscription.getStudent().getFullName();
    }

    private String studentName(TripStudentEntity tripStudent) {
        return tripStudent.getStudent() == null ? "Học sinh" : tripStudent.getStudent().getFullName();
    }

    private String attendanceLabel(AttendanceEventType eventType) {
        return switch (eventType) {
            case BOARDED -> "đã lên xe";
            case DROPPED_OFF -> "đã xuống xe";
            case ABSENT -> "được ghi nhận vắng mặt";
            case NO_SHOW -> "không có mặt tại điểm đón";
            case NOT_SERVED -> "chưa được phục vụ";
        };
    }

    private String appendReason(String message, String reason) {
        return reason == null || reason.isBlank() ? message : message + " Lý do: " + reason.trim();
    }

    private String subscriptionStatusLabel(SubscriptionStatus status) {
        return switch (status) {
            case PENDING -> "đang chờ";
            case ACTIVE -> "đang hoạt động";
            case PAUSED -> "tạm dừng";
            case STOPPED -> "đã dừng";
            case EXPIRED -> "hết hạn";
        };
    }

    private RouteAssignmentEntity currentAssignment(TripExecutionEntity trip) {
        if (trip == null || trip.getRoute() == null || trip.getRoute().getId() == null) {
            return null;
        }
        return routeAssignmentRepository.findCurrentByRoute(
                        trip.getRoute().getId(),
                        trip.getTenantId(),
                        CURRENT_ASSIGNMENT_STATUSES,
                        PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void safely(String eventType, Object entity, Runnable action) {
        Long tenantId = null;
        Long entityId = null;
        if (entity instanceof TransportRequestEntity request) {
            tenantId = request.getTenantId();
            entityId = request.getId();
        } else if (entity instanceof StudentSubscriptionEntity subscription) {
            tenantId = subscription.getTenantId();
            entityId = subscription.getId();
        } else if (entity instanceof RouteAssignmentEntity assignment) {
            tenantId = assignment.getTenantId();
            entityId = assignment.getId();
        } else if (entity instanceof TripExecutionEntity trip) {
            tenantId = trip.getTenantId();
            entityId = trip.getId();
        }

        try {
            action.run();
        } catch (Exception exception) {
            log.error(
                    "School Bus notification orchestration failed: eventType={}, tenantId={}, entityId={}",
                    eventType,
                    tenantId,
                    entityId,
                    exception);
        }
    }
}
