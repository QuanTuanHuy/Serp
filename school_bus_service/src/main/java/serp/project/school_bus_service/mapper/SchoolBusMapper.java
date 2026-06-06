package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.response.*;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.shared.base.BaseMapper;

import java.util.List;

/**
 * Backward-compatible facade that delegates to focused mapper components.
 * Existing code injecting SchoolBusMapper continues to work without changes.
 */
@Component
public class SchoolBusMapper extends BaseMapper {

    private final MasterDataMapper masterDataMapper;
    private final RouteMapper routeMapper;
    private final TripMapper tripMapper;
    private final TransportMapper transportMapper;
    private final OperationsMapper operationsMapper;

    public SchoolBusMapper(MasterDataMapper masterDataMapper, RouteMapper routeMapper,
            TripMapper tripMapper, TransportMapper transportMapper, OperationsMapper operationsMapper) {
        this.masterDataMapper = masterDataMapper;
        this.routeMapper = routeMapper;
        this.tripMapper = tripMapper;
        this.transportMapper = transportMapper;
        this.operationsMapper = operationsMapper;
    }

    // --- Master Data ---
    public SchoolResponse toSchoolResponse(SchoolEntity e) { return masterDataMapper.toSchoolResponse(e); }
    public ParentProfileResponse toParentProfileResponse(ParentProfileEntity e) { return masterDataMapper.toParentProfileResponse(e); }
    public StudentResponse toStudentResponse(StudentEntity e) { return masterDataMapper.toStudentResponse(e); }
    public BusResponse toBusResponse(BusEntity e) { return masterDataMapper.toBusResponse(e); }
    public DriverProfileResponse toDriverProfileResponse(DriverProfileEntity e) { return masterDataMapper.toDriverProfileResponse(e); }
    public AttendantProfileResponse toAttendantProfileResponse(BusAttendantProfileEntity e) { return masterDataMapper.toAttendantProfileResponse(e); }
    public PickupPointResponse toPickupPointResponse(PickupPointEntity e) { return masterDataMapper.toPickupPointResponse(e); }
    public DepotResponse toDepotResponse(DepotEntity e) { return masterDataMapper.toDepotResponse(e); }
    public SchoolScheduleResponse toSchoolScheduleResponse(SchoolScheduleEntity e) { return masterDataMapper.toSchoolScheduleResponse(e); }
    public SchoolPickupPointResponse toSchoolPickupPointResponse(SchoolPickupPointEntity e) { return masterDataMapper.toSchoolPickupPointResponse(e); }
    public SchoolPickupPointWindowResponse toSchoolPickupPointWindowResponse(SchoolPickupPointWindowEntity e) { return masterDataMapper.toSchoolPickupPointWindowResponse(e); }

    // --- Route ---
    public RoutePlanResponse toRoutePlanResponse(RoutePlanEntity e) { return routeMapper.toRoutePlanResponse(e); }
    public RouteStopResponse toRouteStopResponse(RouteStopEntity e) { return routeMapper.toRouteStopResponse(e); }
    public RouteAssignmentResponse toRouteAssignmentResponse(RouteAssignmentEntity e) { return routeMapper.toRouteAssignmentResponse(e); }
    public RoutePlanStudentResponse toRoutePlanStudentResponse(RoutePlanStudentEntity e) { return routeMapper.toRoutePlanStudentResponse(e); }
    public RouteDetailResponse toRouteDetailResponse(RoutePlanEntity r, List<RouteStopEntity> s, List<RoutePlanStudentEntity> ps, RouteAssignmentEntity a) { return routeMapper.toRouteDetailResponse(r, s, ps, a); }
    public RouteDetailResponse toRouteDetailResponse(RoutePlanEntity r, List<RouteStopEntity> s, RouteAssignmentEntity a) { return routeMapper.toRouteDetailResponse(r, s, a); }

    // --- Trip ---
    public TripHistoryResponse toTripHistoryResponse(TripHistoryEntity e) { return tripMapper.toTripHistoryResponse(e); }
    public TripExecutionResponse toTripExecutionResponse(TripExecutionEntity e, List<TripStopLogEntity> s, List<TripStudentEntity> st) { return tripMapper.toTripExecutionResponse(e, s, st); }
    public TripStopLogResponse toTripStopLogResponse(TripStopLogEntity e) { return tripMapper.toTripStopLogResponse(e); }
    public TripStudentResponse toTripStudentResponse(TripStudentEntity e) { return tripMapper.toTripStudentResponse(e); }

    // --- Transport ---
    public RequestStudentResponse toRequestStudentResponse(RequestStudentEntity e) { return transportMapper.toRequestStudentResponse(e); }
    public TransportRequestResponse toTransportRequestResponse(TransportRequestEntity e) { return transportMapper.toTransportRequestResponse(e); }
    public TransportRequestDetailResponse toTransportRequestDetailResponse(TransportRequestEntity e, List<RequestStudentEntity> s) { return transportMapper.toTransportRequestDetailResponse(e, s); }
    public StudentSubscriptionResponse toStudentSubscriptionResponse(StudentSubscriptionEntity e) { return transportMapper.toStudentSubscriptionResponse(e); }
    public TransportRequestHistoryResponse toTransportRequestHistoryResponse(TransportRequestHistoryEntity e) { return transportMapper.toTransportRequestHistoryResponse(e); }
    public StudentSubscriptionHistoryResponse toStudentSubscriptionHistoryResponse(StudentSubscriptionHistoryEntity e) { return transportMapper.toStudentSubscriptionHistoryResponse(e); }
    public SubscriptionPausePeriodResponse toSubscriptionPausePeriodResponse(SubscriptionPausePeriodEntity e) { return transportMapper.toSubscriptionPausePeriodResponse(e); }

    // --- Operations ---
    public AttendanceResponse toAttendanceResponse(AttendanceEntity e) { return operationsMapper.toAttendanceResponse(e); }
    public DemoSessionResponse toDemoSessionResponse(DemoSessionEntity e, List<DemoEventLogEntity> ev) { return operationsMapper.toDemoSessionResponse(e, ev); }
    public DemoEventLogResponse toDemoEventLogResponse(DemoEventLogEntity e) { return operationsMapper.toDemoEventLogResponse(e); }
}
