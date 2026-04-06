package serp.project.school_bus_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.application.dto.response.BusResponse;
import serp.project.school_bus_service.application.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.application.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.application.dto.response.PickupPointResponse;
import serp.project.school_bus_service.application.dto.response.RequestStudentResponse;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.application.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.application.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;
import serp.project.school_bus_service.application.dto.response.SchoolResponse;
import serp.project.school_bus_service.application.dto.response.StudentResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestDetailResponse;
import serp.project.school_bus_service.application.dto.response.TransportRequestResponse;
import serp.project.school_bus_service.application.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;
import serp.project.school_bus_service.infrastructure.store.model.BusAttendantProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;
import serp.project.school_bus_service.infrastructure.store.model.DriverProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.ParentProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.TransportRequestEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripHistoryEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseMapper;

import java.util.List;

@Component
public class SchoolBusMapper extends BaseMapper {

    public SchoolResponse toSchoolResponse(SchoolEntity entity) {
        SchoolResponse response = enrich(new SchoolResponse(), entity);
        response.setName(entity.getName());
        response.setCode(entity.getCode());
        response.setAddress(entity.getAddress());
        response.setContactPhone(entity.getContactPhone());
        response.setContactEmail(entity.getContactEmail());
        return response;
    }

    public ParentProfileResponse toParentProfileResponse(ParentProfileEntity entity) {
        ParentProfileResponse response = enrich(new ParentProfileResponse(), entity);
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setEmail(entity.getEmail());
        response.setAddress(entity.getAddress());
        return response;
    }

    public StudentResponse toStudentResponse(StudentEntity entity) {
        StudentResponse response = enrich(new StudentResponse(), entity);
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setParentProfileId(entity.getParentProfile().getId());
        response.setParentProfileName(entity.getParentProfile().getFullName());
        response.setPickupPointId(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getId());
        response.setPickupPointName(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getName());
        response.setFullName(entity.getFullName());
        response.setStudentCode(entity.getStudentCode());
        response.setGrade(entity.getGrade());
        response.setHomeAddress(entity.getHomeAddress());
        return response;
    }

    public BusResponse toBusResponse(BusEntity entity) {
        BusResponse response = enrich(new BusResponse(), entity);
        response.setPlateNumber(entity.getPlateNumber());
        response.setBusType(entity.getBusType());
        response.setCapacity(entity.getCapacity());
        response.setStatus(entity.getStatus());
        return response;
    }

    public DriverProfileResponse toDriverProfileResponse(DriverProfileEntity entity) {
        DriverProfileResponse response = enrich(new DriverProfileResponse(), entity);
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setLicenseNumber(entity.getLicenseNumber());
        response.setStatus(entity.getStatus());
        return response;
    }

    public AttendantProfileResponse toAttendantProfileResponse(BusAttendantProfileEntity entity) {
        AttendantProfileResponse response = enrich(new AttendantProfileResponse(), entity);
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setStatus(entity.getStatus());
        return response;
    }

    public PickupPointResponse toPickupPointResponse(PickupPointEntity entity) {
        PickupPointResponse response = enrich(new PickupPointResponse(), entity);
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setName(entity.getName());
        response.setAddress(entity.getAddress());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        response.setPickupWindowStart(entity.getPickupWindowStart());
        response.setPickupWindowEnd(entity.getPickupWindowEnd());
        return response;
    }

    public RequestStudentResponse toRequestStudentResponse(RequestStudentEntity entity) {
        RequestStudentResponse response = enrich(new RequestStudentResponse(), entity);
        response.setRequestId(entity.getRequest().getId());
        response.setStudentId(entity.getStudent().getId());
        response.setStudentName(entity.getStudent().getFullName());
        response.setPickupPointId(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getId());
        response.setPickupPointName(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getName());
        return response;
    }

    public TransportRequestResponse toTransportRequestResponse(TransportRequestEntity entity) {
        TransportRequestResponse response = enrich(new TransportRequestResponse(), entity);
        response.setParentProfileId(entity.getParentProfile().getId());
        response.setParentProfileName(entity.getParentProfile().getFullName());
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setRequestType(entity.getRequestType().name());
        response.setStatus(entity.getStatus().name());
        response.setEffectiveFrom(entity.getEffectiveFrom());
        response.setEffectiveTo(entity.getEffectiveTo());
        response.setNotes(entity.getNotes());
        response.setApprovedBy(entity.getApprovedBy());
        response.setApprovedAt(entity.getApprovedAt());
        response.setRejectionReason(entity.getRejectionReason());
        return response;
    }

    public TransportRequestDetailResponse toTransportRequestDetailResponse(TransportRequestEntity entity,
            List<RequestStudentEntity> students) {
        TransportRequestDetailResponse response = new TransportRequestDetailResponse();
        response.setRequest(toTransportRequestResponse(entity));
        response.setStudents(mapList(students, this::toRequestStudentResponse));
        return response;
    }

    public RoutePlanResponse toRoutePlanResponse(RoutePlanEntity entity) {
        RoutePlanResponse response = enrich(new RoutePlanResponse(), entity);
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setRouteCode(entity.getRouteCode());
        response.setRouteName(entity.getRouteName());
        response.setServiceDate(entity.getServiceDate());
        response.setShiftType(entity.getShiftType().name());
        response.setStatus(entity.getStatus().name());
        response.setPlannedDistanceKm(entity.getPlannedDistanceKm());
        response.setPlannedDurationMin(entity.getPlannedDurationMin());
        response.setPlanningNotes(entity.getPlanningNotes());
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());
        return response;
    }

    public RouteStopResponse toRouteStopResponse(RouteStopEntity entity) {
        RouteStopResponse response = enrich(new RouteStopResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setPickupPointId(entity.getPickupPoint().getId());
        response.setPickupPointName(entity.getPickupPoint().getName());
        response.setStopOrder(entity.getStopOrder());
        response.setEstimatedStudentCount(entity.getEstimatedStudentCount());
        response.setPlannedArrivalTime(entity.getPlannedArrivalTime());
        response.setPlannedDepartureTime(entity.getPlannedDepartureTime());
        return response;
    }

    public RouteAssignmentResponse toRouteAssignmentResponse(RouteAssignmentEntity entity) {
        if (entity == null) {
            return null;
        }
        RouteAssignmentResponse response = enrich(new RouteAssignmentResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setBusId(entity.getBus().getId());
        response.setBusPlateNumber(entity.getBus().getPlateNumber());
        response.setDriverId(entity.getDriver().getId());
        response.setDriverName(entity.getDriver().getFullName());
        response.setAttendantId(entity.getAttendant() == null ? null : entity.getAttendant().getId());
        response.setAttendantName(entity.getAttendant() == null ? null : entity.getAttendant().getFullName());
        response.setAssignedAt(entity.getAssignedAt());
        return response;
    }

    public RouteDetailResponse toRouteDetailResponse(RoutePlanEntity route, List<RouteStopEntity> stops,
            RouteAssignmentEntity assignment) {
        RouteDetailResponse response = new RouteDetailResponse();
        response.setRoute(toRoutePlanResponse(route));
        response.setStops(mapList(stops, this::toRouteStopResponse));
        response.setAssignment(toRouteAssignmentResponse(assignment));
        return response;
    }

    public AttendanceResponse toAttendanceResponse(AttendanceEntity entity) {
        AttendanceResponse response = enrich(new AttendanceResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setRouteCode(entity.getRoute().getRouteCode());
        response.setStudentId(entity.getStudent().getId());
        response.setStudentName(entity.getStudent().getFullName());
        response.setAttendanceType(entity.getAttendanceType().name());
        response.setStatus(entity.getStatus().name());
        response.setRecordedAt(entity.getRecordedAt());
        response.setRecordedBy(entity.getRecordedBy());
        response.setNotes(entity.getNotes());
        return response;
    }

    public TripHistoryResponse toTripHistoryResponse(TripHistoryEntity entity) {
        TripHistoryResponse response = enrich(new TripHistoryResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setRouteCode(entity.getRouteCode());
        response.setServiceDate(entity.getServiceDate());
        response.setStatus(entity.getStatus());
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());
        response.setBusId(entity.getBus() == null ? null : entity.getBus().getId());
        response.setBusPlateNumber(entity.getBus() == null ? null : entity.getBus().getPlateNumber());
        response.setDriverId(entity.getDriver() == null ? null : entity.getDriver().getId());
        response.setDriverName(entity.getDriver() == null ? null : entity.getDriver().getFullName());
        response.setAttendantId(entity.getAttendant() == null ? null : entity.getAttendant().getId());
        response.setAttendantName(entity.getAttendant() == null ? null : entity.getAttendant().getFullName());
        return response;
    }
}
