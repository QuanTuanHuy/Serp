package serp.project.school_bus_service.service.domain.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.PlanningIssueResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.service.domain.IRouteEligibilityService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Domain policy service — determines which subscriptions are eligible for route planning.
 * No own aggregate table; delegates data access to aggregate services.
 */
@Service
public class RouteEligibilityServiceImpl implements IRouteEligibilityService {

    private static final String OUTBOUND    = "OUTBOUND";
    private static final String PICKUP_DIR  = "PICKUP_TO_SCHOOL";
    private static final String DROPOFF_DIR = "DROPOFF_FROM_SCHOOL";

    private final IStudentSubscriptionService subscriptionService;
    private final ISchoolPickupPointService schoolPickupPointService;
    private final ISchoolPickupPointWindowService windowService;
    private final ISchoolService schoolService;

    public RouteEligibilityServiceImpl(IStudentSubscriptionService subscriptionService,
                                        ISchoolPickupPointService schoolPickupPointService,
                                        ISchoolPickupPointWindowService windowService,
                                        ISchoolService schoolService) {
        this.subscriptionService = subscriptionService;
        this.schoolPickupPointService = schoolPickupPointService;
        this.windowService = windowService;
        this.schoolService = schoolService;
    }

    // ── Public API ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<StudentSubscriptionEntity> findEligible(
            Long schoolId, Long schoolScheduleId, String routeDirection,
            LocalDate serviceDate, Long tenantId) {

        boolean isOutbound = OUTBOUND.equalsIgnoreCase(routeDirection);

        // 1. DB-level filtering: status, date range, day-of-week, trip direction, point not null
        int dayIndex = dayOfWeekIndex(serviceDate.getDayOfWeek());
        List<TripOption> allowedTrips = isOutbound
                ? List.of(TripOption.MORNING, TripOption.ROUND_TRIP)
                : List.of(TripOption.AFTERNOON, TripOption.ROUND_TRIP);

        List<StudentSubscriptionEntity> candidates = subscriptionService.findEligibleForPlanning(
                schoolId, tenantId, serviceDate, dayIndex, allowedTrips, isOutbound);

        if (candidates.isEmpty()) return List.of();

        // 2. Batch pause-period check (single query instead of N queries)
        List<Long> candidateIds = candidates.stream().map(StudentSubscriptionEntity::getId).toList();
        Set<Long> pausedIds = new HashSet<>(subscriptionService.findPausedSubscriptionIds(
                candidateIds, tenantId, serviceDate));

        List<StudentSubscriptionEntity> notPaused = candidates.stream()
                .filter(s -> !pausedIds.contains(s.getId()))
                .toList();

        if (notPaused.isEmpty()) return List.of();

        // 3. Batch window check: collect all relevant point IDs, then single query
        List<Long> pointIds = notPaused.stream()
                .map(s -> {
                    PickupPointEntity pt = isOutbound ? s.getPickupPoint() : s.getDropoffPoint();
                    return pt != null ? pt.getId() : null;
                })
                .filter(id -> id != null)
                .distinct()
                .toList();

        String expectedDir = isOutbound ? PICKUP_DIR : DROPOFF_DIR;
        Set<Long> pointsWithWindow = new HashSet<>(windowService.findPointIdsWithWindow(
                schoolId, pointIds, schoolScheduleId, expectedDir, tenantId));

        return notPaused.stream()
                .filter(s -> {
                    PickupPointEntity pt = isOutbound ? s.getPickupPoint() : s.getDropoffPoint();
                    return pt != null && pointsWithWindow.contains(pt.getId());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanningPreviewResponse buildPreview(
            Long schoolId, Long schoolScheduleId, String routeDirection,
            LocalDate serviceDate, Long tenantId) {

        var school = schoolService.getSchool(schoolId, tenantId);
        var eligible = findEligible(schoolId, schoolScheduleId, routeDirection, serviceDate, tenantId);
        boolean isOutbound = OUTBOUND.equalsIgnoreCase(routeDirection);

        // Group by relevant point
        Map<Long, PlanningPreviewResponse.EligiblePickupPointResponse> pointMap = new LinkedHashMap<>();
        List<PlanningIssueResponse> issues = new ArrayList<>();

        for (StudentSubscriptionEntity sub : eligible) {
            PickupPointEntity point = isOutbound ? sub.getPickupPoint() : sub.getDropoffPoint();
            if (point == null) continue;

            pointMap.computeIfAbsent(point.getId(), id -> {
                var pp = new PlanningPreviewResponse.EligiblePickupPointResponse();
                pp.setPickupPointId(point.getId());
                pp.setPickupPointName(point.getName());
                pp.setLatitude(point.getLatitude());
                pp.setLongitude(point.getLongitude());
                pp.setStudentCount(0);
                // All points in the result already passed the window check
                pp.setHasWindow(true);
                return pp;
            });
            pointMap.get(point.getId()).setStudentCount(
                    pointMap.get(point.getId()).getStudentCount() + 1);
        }

        List<EligibleStudentResponse> studentResponses = eligible.stream()
                .map(s -> toEligibleStudentResponse(s, schoolScheduleId, routeDirection, tenantId))
                .toList();

        PlanningPreviewResponse response = new PlanningPreviewResponse();
        response.setSchoolId(schoolId);
        response.setSchoolName(school.getName());
        response.setSchoolScheduleId(schoolScheduleId);
        response.setServiceDate(serviceDate.toString());
        response.setRouteDirection(routeDirection);
        response.setTotalEligibleStudents(eligible.size());
        response.setTotalEligiblePickupPoints(pointMap.size());
        response.setEligibleStudents(studentResponses);
        response.setEligiblePickupPoints(new ArrayList<>(pointMap.values()));
        response.setIssues(issues);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EligibleStudentResponse toEligibleStudentResponse(
            StudentSubscriptionEntity sub,
            Long schoolScheduleId, String routeDirection, Long tenantId) {

        boolean isOutbound = OUTBOUND.equalsIgnoreCase(routeDirection);
        PickupPointEntity relevantPoint = isOutbound ? sub.getPickupPoint() : sub.getDropoffPoint();

        EligibleStudentResponse r = new EligibleStudentResponse();
        r.setStudentId(sub.getStudent().getId());
        r.setStudentName(sub.getStudent().getFullName());
        r.setStudentCode(sub.getStudent().getStudentCode());
        r.setSubscriptionId(sub.getId());
        r.setSubscriptionCode(sub.getSubscriptionCode());
        r.setTripOption(sub.getTripOption().name());

        if (sub.getPickupPoint() != null) {
            r.setPickupPointId(sub.getPickupPoint().getId());
            r.setPickupPointName(sub.getPickupPoint().getName());
            r.setPickupPointLatitude(sub.getPickupPoint().getLatitude());
            r.setPickupPointLongitude(sub.getPickupPoint().getLongitude());
        }
        if (sub.getDropoffPoint() != null) {
            r.setDropoffPointId(sub.getDropoffPoint().getId());
            r.setDropoffPointName(sub.getDropoffPoint().getName());
            r.setDropoffPointLatitude(sub.getDropoffPoint().getLatitude());
            r.setDropoffPointLongitude(sub.getDropoffPoint().getLongitude());
        }
        if (relevantPoint != null) {
            r.setRelevantPointId(relevantPoint.getId());
            r.setRelevantPointName(relevantPoint.getName());
            r.setRelevantPointLatitude(relevantPoint.getLatitude());
            r.setRelevantPointLongitude(relevantPoint.getLongitude());

            // Time window lookup via service
            if (schoolScheduleId != null) {
                String expectedDir = isOutbound ? PICKUP_DIR : DROPOFF_DIR;
                Optional<SchoolPickupPointEntity> link = schoolPickupPointService
                        .findLinkBySchoolAndPickupPoint(sub.getSchool().getId(), relevantPoint.getId(), tenantId);
                if (link.isPresent()) {
                    boolean hasWindow = windowService.hasWindow(link.get().getId(), schoolScheduleId, expectedDir, tenantId);
                    if (hasWindow) {
                        // Window exists — individual time details can be fetched if needed
                        r.setWindowStart(null);
                        r.setWindowEnd(null);
                    }
                }
            }
        }
        if (sub.getStudent().getSpecialNote() != null) {
            r.setSpecialNote(sub.getStudent().getSpecialNote());
        }
        return r;
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    /**
     * Maps DayOfWeek to 1-based index used by the JPQL query (1=MON..7=SUN).
     */
    private int dayOfWeekIndex(DayOfWeek day) {
        return day.getValue(); // ISO-8601: MON=1, SUN=7
    }
}
