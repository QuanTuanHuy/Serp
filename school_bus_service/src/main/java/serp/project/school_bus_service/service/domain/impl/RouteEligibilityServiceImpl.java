package serp.project.school_bus_service.service.domain.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest;
import serp.project.school_bus_service.dto.response.*;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointWindowEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.entity.SchoolScheduleDayEntity;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.PlanningMethod;
import serp.project.school_bus_service.service.domain.IRouteEligibilityService;
import serp.project.school_bus_service.service.ISchoolPickupPointService;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.service.IDepotService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ISchoolScheduleService schoolScheduleService;
    private final IDepotService depotService;

    public RouteEligibilityServiceImpl(IStudentSubscriptionService subscriptionService,
                                        ISchoolPickupPointService schoolPickupPointService,
                                        ISchoolPickupPointWindowService windowService,
                                        ISchoolService schoolService,
                                        ISchoolScheduleService schoolScheduleService,
                                        IDepotService depotService) {
        this.subscriptionService = subscriptionService;
        this.schoolPickupPointService = schoolPickupPointService;
        this.windowService = windowService;
        this.schoolService = schoolService;
        this.schoolScheduleService = schoolScheduleService;
        this.depotService = depotService;
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
            PlanningSessionPreviewRequest request, Long tenantId) {

        Long schoolId = request.getSchoolId();
        Long schoolScheduleId = request.getSchoolScheduleId();
        LocalDate serviceDate = request.getServiceDate();
        String routeDirection = request.getRouteDirection();
        String planningMethod = request.getPlanningMethod();
        Long depotId = request.getDepotId();

        boolean isOutbound = OUTBOUND.equalsIgnoreCase(routeDirection);

        // Load School & Schedule
        SchoolEntity school = schoolService.getSchool(schoolId, tenantId);
        SchoolScheduleEntity schedule = schoolScheduleService.getSchedule(schoolScheduleId, tenantId);

        // Load Depot
        DepotEntity depot = null;
        if (depotId != null) {
            depot = depotService.getDepot(depotId, tenantId);
        }

        // 1. Get all candidate subscriptions for this school
        List<StudentSubscriptionEntity> allSubscriptions = subscriptionService.findAllBySchoolIdAndTenantId(schoolId, tenantId);

        // 2. Batch check paused subscriptions
        List<Long> activeCandidateIds = allSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .map(StudentSubscriptionEntity::getId)
                .toList();
        Set<Long> pausedSubscriptionIds = new HashSet<>();
        if (!activeCandidateIds.isEmpty()) {
            pausedSubscriptionIds.addAll(
                subscriptionService.findPausedSubscriptionIds(activeCandidateIds, tenantId, serviceDate)
            );
        }

        // 3. Batch load school-pickup points and active links
        List<SchoolPickupPointEntity> schoolLinks = schoolPickupPointService.getPickupPointLinksForSchools(List.of(schoolId), tenantId);
        Set<Long> activeLinkedPointIds = schoolLinks.stream()
                .filter(link -> Boolean.TRUE.equals(link.getIsActive()))
                .map(link -> link.getPickupPoint().getId())
                .collect(Collectors.toSet());

        // Map schoolLink ID -> pickupPoint ID
        Map<Long, Long> linkIdToPointId = schoolLinks.stream()
                .collect(Collectors.toMap(SchoolPickupPointEntity::getId, link -> link.getPickupPoint().getId(), (a, b) -> a));

        // 4. Batch load pickup windows for these links and schedule
        List<Long> linkIds = schoolLinks.stream().map(SchoolPickupPointEntity::getId).toList();
        List<SchoolPickupPointWindowEntity> windows = new ArrayList<>();
        if (!linkIds.isEmpty()) {
            windows.addAll(windowService.getWindowsForLinks(linkIds, tenantId));
        }

        // Filter windows by schedule and direction
        String expectedDir = isOutbound ? PICKUP_DIR : DROPOFF_DIR;
        Map<Long, SchoolPickupPointWindowEntity> pointToWindowMap = new HashMap<>();
        for (SchoolPickupPointWindowEntity w : windows) {
            if (w.getSchoolSchedule().getId().equals(schoolScheduleId) && expectedDir.equalsIgnoreCase(w.getDirection())) {
                Long pointId = linkIdToPointId.get(w.getSchoolPickupPoint().getId());
                if (pointId != null) {
                    pointToWindowMap.put(pointId, w);
                }
            }
        }

        List<PlanningDemandResponse> eligibleDemands = new ArrayList<>();
        List<PlanningDemandResponse> blockedDemands = new ArrayList<>();
        List<PlanningReadinessIssueResponse> issues = new ArrayList<>();

        // Group counts for summary
        int missingCoordinateCount = 0;
        int missingWindowCount = 0;
        int pausedCount = 0;
        int inactiveCount = 0;
        int outOfEffectiveRangeCount = 0;
        int dayMismatchCount = 0;

        DayOfWeek dayOfWeek = serviceDate.getDayOfWeek();

        for (StudentSubscriptionEntity sub : allSubscriptions) {
            List<String> subIssueCodes = new ArrayList<>();
            List<String> subIssueLabels = new ArrayList<>();
            boolean isBlocked = false;

            // Rule 4.2: Subscription status
            if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
                isBlocked = true;
                inactiveCount++;
                String code = "SUBSCRIPTION_NOT_ACTIVE";
                String label = "Subscription is not active";
                if (sub.getStatus() == SubscriptionStatus.PAUSED) {
                    code = "SUBSCRIPTION_PAUSED";
                    label = "Subscription is paused";
                } else if (sub.getStatus() == SubscriptionStatus.STOPPED) {
                    code = "SUBSCRIPTION_STOPPED";
                    label = "Subscription is stopped";
                } else if (sub.getStatus() == SubscriptionStatus.EXPIRED) {
                    code = "SUBSCRIPTION_EXPIRED";
                    label = "Subscription is expired";
                }
                subIssueCodes.add(code);
                subIssueLabels.add(label);
            }

            // Rule 4.3: Pause period
            if (sub.getStatus() == SubscriptionStatus.ACTIVE && pausedSubscriptionIds.contains(sub.getId())) {
                isBlocked = true;
                pausedCount++;
                subIssueCodes.add("SUBSCRIPTION_PAUSED_ON_SERVICE_DATE");
                subIssueLabels.add("Subscription is paused on service date");
            }

            // Rule 4.4: Effective date range
            if (sub.getEffectiveFrom().isAfter(serviceDate) ||
                    (sub.getEffectiveTo() != null && sub.getEffectiveTo().isBefore(serviceDate))) {
                isBlocked = true;
                outOfEffectiveRangeCount++;
                subIssueCodes.add("OUT_OF_EFFECTIVE_RANGE");
                subIssueLabels.add("Service date is outside effective period");
            }

            // Rule 4.5: Active day
            boolean activeOnDay = switch (dayOfWeek) {
                case MONDAY -> Boolean.TRUE.equals(sub.getMonday());
                case TUESDAY -> Boolean.TRUE.equals(sub.getTuesday());
                case WEDNESDAY -> Boolean.TRUE.equals(sub.getWednesday());
                case THURSDAY -> Boolean.TRUE.equals(sub.getThursday());
                case FRIDAY -> Boolean.TRUE.equals(sub.getFriday());
                case SATURDAY -> Boolean.TRUE.equals(sub.getSaturday());
                case SUNDAY -> Boolean.TRUE.equals(sub.getSunday());
            };
            if (!activeOnDay) {
                isBlocked = true;
                dayMismatchCount++;
                subIssueCodes.add("SERVICE_DAY_NOT_SELECTED");
                subIssueLabels.add("Service day is not selected");
            }

            // Rule 4.6: School + schedule mismatch
            if (sub.getSchool() == null || !sub.getSchool().getId().equals(schoolId)) {
                isBlocked = true;
                subIssueCodes.add("SCHOOL_MISMATCH");
                subIssueLabels.add("School mismatch");
            }
            if (sub.getSchoolSchedule() != null && !sub.getSchoolSchedule().getId().equals(schoolScheduleId)) {
                isBlocked = true;
                subIssueCodes.add("SCHEDULE_MISMATCH");
                subIssueLabels.add("Schedule mismatch");
            }

            // Rule 4.7: Direction + trip option mismatch
            boolean correctTripOption = false;
            if (isOutbound) {
                correctTripOption = sub.getTripOption() == TripOption.MORNING || sub.getTripOption() == TripOption.ROUND_TRIP;
            } else {
                correctTripOption = sub.getTripOption() == TripOption.AFTERNOON || sub.getTripOption() == TripOption.ROUND_TRIP;
            }
            if (!correctTripOption) {
                isBlocked = true;
                subIssueCodes.add("TRIP_OPTION_NOT_APPLICABLE_FOR_DIRECTION");
                subIssueLabels.add("Trip option is not applicable for selected direction");
            }

            // Rule 4.8: Required point
            PickupPointEntity point = isOutbound ? sub.getPickupPoint() : sub.getDropoffPoint();
            if (point == null) {
                isBlocked = true;
                if (isOutbound) {
                    subIssueCodes.add("MISSING_PICKUP_POINT");
                    subIssueLabels.add("Missing pickup point");
                } else {
                    subIssueCodes.add("MISSING_DROPOFF_POINT");
                    subIssueLabels.add("Missing drop-off point");
                }
            } else {
                // Rule 4.9: Point linked with school
                if (!activeLinkedPointIds.contains(point.getId())) {
                    isBlocked = true;
                    subIssueCodes.add("POINT_NOT_LINKED_WITH_SCHOOL");
                    subIssueLabels.add("Point is not linked with school");
                }

                // Rule 4.10: Coordinate availability
                if (point.getLatitude() == null || point.getLongitude() == null) {
                    isBlocked = true;
                    missingCoordinateCount++;
                    subIssueCodes.add("MISSING_POINT_COORDINATES");
                    subIssueLabels.add("Missing point coordinates");
                }

                // Rule 4.11: Window validation
                SchoolPickupPointWindowEntity w = pointToWindowMap.get(point.getId());
                if (w == null) {
                    isBlocked = true;
                    missingWindowCount++;
                    if (isOutbound) {
                        subIssueCodes.add("MISSING_PICKUP_WINDOW");
                        subIssueLabels.add("Missing pickup window");
                    } else {
                        subIssueCodes.add("MISSING_DROPOFF_WINDOW");
                        subIssueLabels.add("Missing drop-off window");
                    }
                }
            }

            // Create Demand response
            PlanningDemandResponse demand = new PlanningDemandResponse();
            demand.setSubscriptionId(sub.getId());
            demand.setSubscriptionCode(sub.getSubscriptionCode());
            demand.setStudentId(sub.getStudent().getId());
            demand.setStudentCode(sub.getStudent().getStudentCode());
            demand.setStudentName(sub.getStudent().getFullName());
            demand.setSchoolId(schoolId);
            demand.setSchoolName(school.getName());
            demand.setSchoolScheduleId(schoolScheduleId);
            demand.setScheduleCode(schedule.getScheduleCode());
            demand.setScheduleName(schedule.getScheduleName());
            demand.setTripOption(sub.getTripOption().name());
            demand.setTripOptionLabel(sub.getTripOption().name());

            if (point != null) {
                demand.setPointId(point.getId());
                demand.setPointCode(point.getCode());
                demand.setPointName(point.getName());
                if (point.getLatitude() != null) demand.setLatitude(BigDecimal.valueOf(point.getLatitude()));
                if (point.getLongitude() != null) demand.setLongitude(BigDecimal.valueOf(point.getLongitude()));

                SchoolPickupPointWindowEntity w = pointToWindowMap.get(point.getId());
                if (w != null) {
                    demand.setWindowStart(w.getWindowStart());
                    demand.setWindowEnd(w.getWindowEnd());
                }
            }

            if (isBlocked) {
                demand.setReadinessStatus("BLOCKED");
                if (!subIssueCodes.isEmpty()) {
                    demand.setReasonCode(subIssueCodes.get(0));
                    demand.setReasonLabel(subIssueLabels.get(0));
                }
                demand.setIssueCodes(subIssueCodes);
                demand.setIssueLabels(subIssueLabels);
                blockedDemands.add(demand);

                // Add to global issues
                for (int i = 0; i < subIssueCodes.size(); i++) {
                    PlanningReadinessIssueResponse issue = new PlanningReadinessIssueResponse();
                    issue.setSeverity("BLOCKING");
                    issue.setCode(subIssueCodes.get(i));
                    issue.setLabel(subIssueLabels.get(i));
                    issue.setSubscriptionId(sub.getId());
                    issue.setStudentId(sub.getStudent().getId());
                    if (point != null) issue.setPointId(point.getId());
                    issues.add(issue);
                }
            } else {
                demand.setReadinessStatus("READY");
                demand.setReasonCode("READY");
                demand.setReasonLabel("Ready");
                demand.setIssueCodes(List.of());
                demand.setIssueLabels(List.of());
                eligibleDemands.add(demand);
            }
        }

        // Rule 4.12: Depot Coordinate & Greedy Planning
        if (PlanningMethod.GREEDY.name().equalsIgnoreCase(planningMethod)) {
            if (depotId == null) {
                PlanningReadinessIssueResponse issue = new PlanningReadinessIssueResponse();
                issue.setSeverity("BLOCKING");
                issue.setCode("DEPOT_REQUIRED_FOR_GREEDY");
                issue.setLabel("Depot is required for greedy planning");
                issues.add(issue);
            } else if (depot == null) {
                PlanningReadinessIssueResponse issue = new PlanningReadinessIssueResponse();
                issue.setSeverity("BLOCKING");
                issue.setCode("DEPOT_NOT_FOUND");
                issue.setLabel("Depot not found");
                issues.add(issue);
            } else if (depot.getLatitude() == null || depot.getLongitude() == null) {
                PlanningReadinessIssueResponse issue = new PlanningReadinessIssueResponse();
                issue.setSeverity("BLOCKING");
                issue.setCode("DEPOT_MISSING_COORDINATES");
                issue.setLabel("Depot is missing coordinates");
                issues.add(issue);
            }
        }

        // Group Points response
        Map<Long, PlanningPointResponse> pointResponseMap = new HashMap<>();
        List<PlanningDemandResponse> allDemands = new ArrayList<>();
        allDemands.addAll(eligibleDemands);
        allDemands.addAll(blockedDemands);

        for (PlanningDemandResponse d : allDemands) {
            if (d.getPointId() == null) continue;
            pointResponseMap.computeIfAbsent(d.getPointId(), id -> {
                PlanningPointResponse pr = new PlanningPointResponse();
                pr.setPointId(d.getPointId());
                pr.setPointCode(d.getPointCode());
                pr.setPointName(d.getPointName());
                pr.setLatitude(d.getLatitude());
                pr.setLongitude(d.getLongitude());
                pr.setPointRole(isOutbound ? "PICKUP" : "DROPOFF");
                pr.setWindowStart(d.getWindowStart());
                pr.setWindowEnd(d.getWindowEnd());
                pr.setStudentCount(0);
                pr.setIssueLabels(new ArrayList<>());
                pr.setReadinessStatus("READY");
                return pr;
            });

            PlanningPointResponse pr = pointResponseMap.get(d.getPointId());
            pr.setStudentCount(pr.getStudentCount() + 1);
            if ("BLOCKED".equals(d.getReadinessStatus())) {
                pr.setReadinessStatus("BLOCKED");
                if (d.getIssueLabels() != null) {
                    for (String label : d.getIssueLabels()) {
                        if (!pr.getIssueLabels().contains(label)) {
                            pr.getIssueLabels().add(label);
                        }
                    }
                }
            }
        }

        // Compute summary
        PlanningReadinessSummary summary = new PlanningReadinessSummary();
        summary.setTotalSubscriptions(allSubscriptions.size());
        summary.setEligibleStudents(eligibleDemands.size());
        summary.setBlockedStudents(blockedDemands.size());
        summary.setWarningStudents(0);
        summary.setPointCount(pointResponseMap.size());
        summary.setPickupPointCount(isOutbound ? pointResponseMap.size() : 0);
        summary.setDropoffPointCount(isOutbound ? 0 : pointResponseMap.size());
        summary.setMissingCoordinateCount(missingCoordinateCount);
        summary.setMissingWindowCount(missingWindowCount);
        summary.setPausedCount(pausedCount);
        summary.setInactiveCount(inactiveCount);
        summary.setOutOfEffectiveRangeCount(outOfEffectiveRangeCount);
        summary.setDayMismatchCount(dayMismatchCount);

        // Build Response
        PlanningPreviewResponse response = new PlanningPreviewResponse();
        response.setSchoolId(schoolId);
        response.setSchoolCode(school.getCode());
        response.setSchoolName(school.getName());
        response.setSchoolAddress(school.getAddress());

        response.setSchoolScheduleId(schoolScheduleId);
        response.setScheduleCode(schedule.getScheduleCode());
        response.setScheduleName(schedule.getScheduleName());
        response.setShiftType(schedule.getShiftType());
        response.setArrivalDeadline(schedule.getArrivalDeadline());
        response.setDepartureTime(schedule.getDepartureTime());
        response.setEffectiveFrom(schedule.getEffectiveFrom());
        response.setEffectiveTo(schedule.getEffectiveTo());

        List<DayOfWeek> activeDays = new ArrayList<>();
        if (schedule.getScheduleDays() != null) {
            for (SchoolScheduleDayEntity d : schedule.getScheduleDays()) {
                try {
                    activeDays.add(DayOfWeek.valueOf(d.getDayOfWeek().toUpperCase()));
                } catch (Exception ignored) {}
            }
        }
        response.setActiveDays(activeDays);

        response.setServiceDate(serviceDate);
        response.setServiceDayOfWeek(dayOfWeek);
        response.setDirection(routeDirection);
        response.setPlanningMethod(planningMethod);

        if (depot != null) {
            response.setDepotId(depot.getId());
            response.setDepotCode(depot.getCode());
            response.setDepotName(depot.getName());
        }
        response.setDefaultBusCapacity(request.getDefaultBusCapacity());

        response.setSummary(summary);
        response.setEligibleDemands(eligibleDemands);
        response.setBlockedDemands(blockedDemands);
        response.setPoints(new ArrayList<>(pointResponseMap.values()));
        response.setIssues(issues);

        // Backward compatibility
        response.setSchoolScheduleName(schedule.getScheduleName());
        response.setRouteDirection(routeDirection);
        response.setTotalEligibleStudents(eligibleDemands.size());
        response.setTotalEligiblePickupPoints(pointResponseMap.size());

        // Mapping to EligibleStudentResponse
        List<EligibleStudentResponse> legacyEligible = eligibleDemands.stream()
                .map(d -> {
                    EligibleStudentResponse es = new EligibleStudentResponse();
                    es.setStudentId(d.getStudentId());
                    es.setStudentName(d.getStudentName());
                    es.setStudentCode(d.getStudentCode());
                    es.setSubscriptionId(d.getSubscriptionId());
                    es.setSubscriptionCode(d.getSubscriptionCode());
                    es.setTripOption(d.getTripOption());
                    es.setRelevantPointId(d.getPointId());
                    es.setRelevantPointName(d.getPointName());
                    if (d.getLatitude() != null) es.setRelevantPointLatitude(d.getLatitude().doubleValue());
                    if (d.getLongitude() != null) es.setRelevantPointLongitude(d.getLongitude().doubleValue());
                    es.setWindowStart(d.getWindowStart());
                    es.setWindowEnd(d.getWindowEnd());
                    return es;
                }).toList();
        response.setEligibleStudents(legacyEligible);

        // Mapping to EligiblePickupPointResponse
        List<PlanningPreviewResponse.EligiblePickupPointResponse> legacyPoints = pointResponseMap.values().stream()
                .filter(p -> "READY".equals(p.getReadinessStatus()))
                .map(p -> {
                    PlanningPreviewResponse.EligiblePickupPointResponse epp = new PlanningPreviewResponse.EligiblePickupPointResponse();
                    epp.setPickupPointId(p.getPointId());
                    epp.setPickupPointName(p.getPointName());
                    if (p.getLatitude() != null) epp.setLatitude(p.getLatitude().doubleValue());
                    if (p.getLongitude() != null) epp.setLongitude(p.getLongitude().doubleValue());
                    epp.setStudentCount(p.getStudentCount());
                    epp.setHasWindow(p.getWindowStart() != null);
                    return epp;
                }).toList();
        response.setEligiblePickupPoints(legacyPoints);

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
