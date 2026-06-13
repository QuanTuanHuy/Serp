package serp.project.school_bus_service.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Application error code registry.
 * Errors are grouped by functional domain using static inner classes.
 * Each constant is an {@link ErrorInfo} record holding the i18n message key,
 * a default English message, and the HTTP status to return.
 */
public class AppErrorCode {

    /** Immutable value object carried by {@link AppException}. */
    public record ErrorInfo(String code, String defaultMessage, HttpStatus status) {}

    // ── System / Infrastructure ───────────────────────────────────────────────
    public static final ErrorInfo UNEXPECTED_EXCEPTION      = new ErrorInfo("system.unexpectedException",     "An unexpected error occurred.",       HttpStatus.INTERNAL_SERVER_ERROR);
    public static final ErrorInfo NOT_FOUND                 = new ErrorInfo("system.notFound",                "Entity not found or access denied.",  HttpStatus.NOT_FOUND);
    public static final ErrorInfo DATA_INTEGRITY_VIOLATION  = new ErrorInfo("system.dataIntegrityViolation",  "Data integrity violation.",           HttpStatus.BAD_REQUEST);
    public static final ErrorInfo REQUEST_VALIDATION_FAILED = new ErrorInfo("system.requestValidationFailed", "Request validation failed.",          HttpStatus.BAD_REQUEST);
    public static final ErrorInfo UNAUTHORIZED              = new ErrorInfo("system.unauthorized",            "Unauthorized access.",                HttpStatus.UNAUTHORIZED);
    public static final ErrorInfo FORBIDDEN                 = new ErrorInfo("system.forbidden",               "Forbidden resource access.",          HttpStatus.FORBIDDEN);
    /** Used by enum {@code parse()} methods which cannot inject Spring beans. */
    public static final ErrorInfo INVALID_ENUM_VALUE        = new ErrorInfo("system.invalidEnumValue",        "Invalid value provided.",             HttpStatus.BAD_REQUEST);

    // ── Bus ───────────────────────────────────────────────────────────────────
    public static class Bus {
        public static final ErrorInfo INVALID_STATUS        = new ErrorInfo("bus.status.invalid",         "Invalid bus status.",              HttpStatus.BAD_REQUEST);
        public static final ErrorInfo HOME_DEPOT_REQUIRED   = new ErrorInfo("bus.homeDepot.required",     "Home depot is required.",          HttpStatus.BAD_REQUEST);
        public static final ErrorInfo PLATE_NUMBER_CONFLICT = new ErrorInfo("bus.plateNumber.conflict",   "Plate number already exists.",     HttpStatus.CONFLICT);
        public static final ErrorInfo DELETED               = new ErrorInfo("bus.deleted",                "Bus has been deleted.",            HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INACTIVE              = new ErrorInfo("bus.inactive",               "Bus is not active.",               HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NOT_AVAILABLE         = new ErrorInfo("bus.notAvailable",           "Bus is not available.",            HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CAPACITY_EXCEEDED     = new ErrorInfo("bus.capacity.exceeded",      "Bus capacity exceeded.",           HttpStatus.BAD_REQUEST);
        public static final ErrorInfo SELECTED_BUS_REQUIRED = new ErrorInfo("bus.selectedBus.required", "Please select a bus before adding students to the route.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CAPACITY_NOT_CONFIGURED = new ErrorInfo("bus.capacity.notConfigured", "The selected bus does not have capacity configured.", HttpStatus.BAD_REQUEST);
        private Bus() {}
    }

    // ── Driver ────────────────────────────────────────────────────────────────
    public static class Driver {
        public static final ErrorInfo INVALID_STATUS    = new ErrorInfo("driver.status.invalid",        "Invalid driver status.",            HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DELETED           = new ErrorInfo("driver.deleted",               "Driver has been deleted.",          HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INACTIVE          = new ErrorInfo("driver.inactive",              "Driver is not active.",             HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NOT_AVAILABLE     = new ErrorInfo("driver.notAvailable",          "Driver is not available.",          HttpStatus.BAD_REQUEST);
        private Driver() {}
    }

    // ── Attendant ─────────────────────────────────────────────────────────────
    public static class Attendant {
        public static final ErrorInfo INVALID_STATUS = new ErrorInfo("attendant.status.invalid", "Invalid attendant status.",    HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DELETED        = new ErrorInfo("attendant.deleted",        "Attendant has been deleted.",  HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INACTIVE       = new ErrorInfo("attendant.inactive",       "Attendant is not active.",     HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NOT_AVAILABLE  = new ErrorInfo("attendant.notAvailable",   "Attendant is not available.",  HttpStatus.BAD_REQUEST);
        private Attendant() {}
    }

    // ── Parent ────────────────────────────────────────────────────────────────
    public static class Parent {
        public static final ErrorInfo PHONE_REQUIRED = new ErrorInfo("parent.phone.required", "Parent phone is required.",       HttpStatus.BAD_REQUEST);
        public static final ErrorInfo EMAIL_INVALID  = new ErrorInfo("parent.email.invalid",  "Parent email format is invalid.", HttpStatus.BAD_REQUEST);
        private Parent() {}
    }

    // ── Student ───────────────────────────────────────────────────────────────
    public static class Student {
        public static final ErrorInfo DOB_FUTURE = new ErrorInfo("student.dob.future", "Date of birth cannot be in the future.", HttpStatus.BAD_REQUEST);
        private Student() {}
    }

    // ── School ────────────────────────────────────────────────────────────────
    public static class School {
        public static final ErrorInfo EMAIL_INVALID = new ErrorInfo("school.email.invalid", "Contact email format is invalid.", HttpStatus.BAD_REQUEST);
        private School() {}
    }

    // ── Coordinate (shared across depot, school, pickup point) ────────────────
    public static class Coordinate {
        public static final ErrorInfo BOTH_REQUIRED   = new ErrorInfo("coordinate.bothRequired",   "Both latitude and longitude are required.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo LATITUDE_RANGE  = new ErrorInfo("coordinate.latitudeRange",  "Latitude is out of range.",                 HttpStatus.BAD_REQUEST);
        public static final ErrorInfo LONGITUDE_RANGE = new ErrorInfo("coordinate.longitudeRange", "Longitude is out of range.",                HttpStatus.BAD_REQUEST);
        private Coordinate() {}
    }

    // ── PickupPoint ───────────────────────────────────────────────────────────
    public static class PickupPoint {
        public static final ErrorInfo USAGE_TYPE_REQUIRED     = new ErrorInfo("pickupPoint.usageType.required",    "Usage type is required.",               HttpStatus.BAD_REQUEST);
        public static final ErrorInfo USAGE_TYPE_INVALID      = new ErrorInfo("pickupPoint.usageType.invalid",     "Invalid usage type.",                   HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INACTIVE                = new ErrorInfo("pickupPoint.inactive",              "Pickup point is not active.",           HttpStatus.BAD_REQUEST);
        public static final ErrorInfo USAGE_NOT_ALLOWED_PICKUP= new ErrorInfo("pickupPoint.usageNotAllowedPickup", "Usage type does not allow pickup.",     HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NOT_LINKED              = new ErrorInfo("pickupPoint.notLinked",             "Pickup point not linked to school.",    HttpStatus.BAD_REQUEST);
        private PickupPoint() {}
    }

    // ── DropoffPoint ──────────────────────────────────────────────────────────
    public static class DropoffPoint {
        public static final ErrorInfo INACTIVE             = new ErrorInfo("dropoffPoint.inactive",              "Drop-off point is not active.",          HttpStatus.BAD_REQUEST);
        public static final ErrorInfo USAGE_NOT_ALLOWED_DROP= new ErrorInfo("dropoffPoint.usageNotAllowedDropoff","Usage type does not allow drop-off.",   HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NOT_LINKED           = new ErrorInfo("dropoffPoint.notLinked",             "Drop-off point not linked to school.",   HttpStatus.BAD_REQUEST);
        private DropoffPoint() {}
    }

    // ── SchoolPickupPoint ─────────────────────────────────────────────────────
    public static class SchoolPickupPoint {
        public static final ErrorInfo CONFLICT       = new ErrorInfo("schoolPickupPoint.conflict",    "Pickup point already linked to this school.",     HttpStatus.CONFLICT);
        public static final ErrorInfo NOT_FOUND      = new ErrorInfo("schoolPickupPoint.notFound",    "School pickup point link not found.",             HttpStatus.NOT_FOUND);
        public static final ErrorInfo LINK_NOT_FOUND = new ErrorInfo("schoolPickupPoint.linkNotFound","Link between school and pickup point not found.", HttpStatus.NOT_FOUND);
        private SchoolPickupPoint() {}
    }

    // ── Window (school pickup point time window) ───────────────────────────────
    public static class Window {
        public static final ErrorInfo NOT_FOUND                   = new ErrorInfo("window.notFound",                    "Window not found.",                                   HttpStatus.NOT_FOUND);
        public static final ErrorInfo SCHOOL_MISMATCH             = new ErrorInfo("window.schoolMismatch",              "Schedule and pickup point must belong to same school.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DIRECTION_INVALID           = new ErrorInfo("window.direction.invalid",           "Invalid window direction.",                           HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DIRECTION_USAGE_MISMATCH    = new ErrorInfo("window.direction.usageTypeMismatch", "Direction incompatible with usage type.",             HttpStatus.BAD_REQUEST);
        public static final ErrorInfo END_BEFORE_START            = new ErrorInfo("window.endBeforeStart",              "Window end must be on or after window start.",        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DISTANCE_NEGATIVE           = new ErrorInfo("window.distance.negative",           "Estimated distance must be >= 0.",                    HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DURATION_NEGATIVE           = new ErrorInfo("window.duration.negative",           "Estimated duration must be >= 0.",                    HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CONFLICT                    = new ErrorInfo("window.conflict",                    "Window already exists for this combination.",         HttpStatus.CONFLICT);
        public static final ErrorInfo PICKUP_AFTER_DEADLINE       = new ErrorInfo("window.pickup.afterDeadline",        "Pickup window must end before arrival deadline.",     HttpStatus.BAD_REQUEST);
        public static final ErrorInfo PICKUP_TRAVEL_EXCEEDS       = new ErrorInfo("window.pickup.travelExceedsDeadline","Pickup travel time exceeds arrival deadline.",        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DROPOFF_BEFORE_DEPARTURE    = new ErrorInfo("window.dropoff.beforeDeparture",     "Drop-off must start after departure time.",           HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DROPOFF_TRAVEL_INSUFFICIENT = new ErrorInfo("window.dropoff.travelInsufficient",  "Insufficient travel time for drop-off.",              HttpStatus.BAD_REQUEST);
        private Window() {}
    }

    // ── Schedule ──────────────────────────────────────────────────────────────
    public static class Schedule {
        public static final ErrorInfo ARRIVAL_DEADLINE_REQUIRED = new ErrorInfo("schedule.arrivalDeadline.required", "Arrival deadline is required.",                         HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DEPARTURE_TIME_REQUIRED   = new ErrorInfo("schedule.departureTime.required",   "Departure time is required.",                           HttpStatus.BAD_REQUEST);
        public static final ErrorInfo EFFECTIVE_DATES_INVALID   = new ErrorInfo("schedule.effectiveDates.invalid",   "Effective to date must be on or after effective from.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DAYS_OF_WEEK_REQUIRED     = new ErrorInfo("schedule.daysOfWeek.required",      "Applicable days are required.",                        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DEFAULT_CONFLICT          = new ErrorInfo("schedule.defaultConflict",           "School already has an active default schedule.",       HttpStatus.CONFLICT);
        public static final ErrorInfo DAY_INVALID               = new ErrorInfo("schedule.dayInvalid",               "Invalid day of week.",                                 HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NOT_FOUND                 = new ErrorInfo("schedule.notFound",                 "School schedule not found.",                           HttpStatus.NOT_FOUND);
        private Schedule() {}
    }

    // ── Map ───────────────────────────────────────────────────────────────────
    public static class Map {
        public static final ErrorInfo QUERY_REQUIRED       = new ErrorInfo("map.query.required",       "Search query is required.",                   HttpStatus.BAD_REQUEST);
        public static final ErrorInfo COORDINATES_REQUIRED = new ErrorInfo("map.coordinates.required", "Latitude and longitude are required.",        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ADDRESS_NOT_FOUND    = new ErrorInfo("map.address.notFound",     "No address found for the selected location.", HttpStatus.NOT_FOUND);
        private Map() {}
    }

    // ── Attendance ────────────────────────────────────────────────────────────
    public static class Attendance {
        public static final ErrorInfo ALREADY_BOARDED    = new ErrorInfo("attendance.alreadyBoarded",   "Student has already boarded.",        HttpStatus.CONFLICT);
        public static final ErrorInfo MUST_BOARD_FIRST   = new ErrorInfo("attendance.mustBoardFirst",   "Must board before dropoff.",          HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STUDENT_NOT_IN_TRIP= new ErrorInfo("attendance.studentNotInTrip", "Student is not in this trip.",        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INVALID_STATE      = new ErrorInfo("attendance.invalidState",     "Invalid attendance state.",           HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INVALID_REQUEST        = new ErrorInfo("attendance.invalidRequest",       "Invalid attendance request.",                                          HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STOP_NOT_ACTIVE         = new ErrorInfo("attendance.stopNotActive",        "Cannot record attendance for a departed or skipped stop.",             HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STUDENT_STATUS_INVALID  = new ErrorInfo("attendance.studentStatusInvalid", "Student status does not allow this action.",                          HttpStatus.BAD_REQUEST);
        private Attendance() {}
    }

    // ── RouteStop ─────────────────────────────────────────────────────────────
    public static class RouteStop {
        public static final ErrorInfo COUNT_MISMATCH           = new ErrorInfo("routeStop.countMismatch",          "Stop count mismatch.",                      HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NOT_FOUND                = new ErrorInfo("routeStop.notFound",               "Stop not found.",                           HttpStatus.NOT_FOUND);
        public static final ErrorInfo SESSION_NOT_LINKED       = new ErrorInfo("routeStop.sessionNotLinked",       "Route is not linked to a planning session.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo SESSION_FROZEN           = new ErrorInfo("routeStop.sessionFrozen",          "Planning session is frozen.",               HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STUDENT_MISMATCH        = new ErrorInfo("routeStop.studentMismatch",        "Student does not match subscription.",       HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NO_PICKUP_POINT         = new ErrorInfo("routeStop.noPickupPointConfigured","No pickup point configured.",               HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STUDENT_ALREADY_ASSIGNED= new ErrorInfo("routeStop.studentAlreadyAssigned", "Student is already assigned to this route. Please refresh and try again.", HttpStatus.CONFLICT);
        public static final ErrorInfo INVALID_REQUEST         = new ErrorInfo("routeStop.invalidRequest",         "Invalid route stop request.",               HttpStatus.BAD_REQUEST);
        private RouteStop() {}
    }

    // ── Route ─────────────────────────────────────────────────────────────────
    public static class Route {
        public static final ErrorInfo OUTBOUND_MUST_END_SCHOOL  = new ErrorInfo("route.outbound.mustEndAtSchool",  "Outbound routes must end at the school.",  HttpStatus.BAD_REQUEST);
        public static final ErrorInfo RETURN_MUST_START_SCHOOL  = new ErrorInfo("route.return.mustStartAtSchool",  "Return routes must start at the school.",  HttpStatus.BAD_REQUEST);
        public static final ErrorInfo DEPOT_REQUIRED            = new ErrorInfo("route.depot.required",            "Depot is required.",                       HttpStatus.BAD_REQUEST);
        public static final ErrorInfo SCHOOL_REQUIRED           = new ErrorInfo("route.school.required",           "School is required.",                      HttpStatus.BAD_REQUEST);
        public static final ErrorInfo SCHOOL_MISMATCH           = new ErrorInfo("route.school.mismatch",           "School mismatch.",                         HttpStatus.BAD_REQUEST);
        public static final ErrorInfo FIELD_INVALID             = new ErrorInfo("route.field.invalid",             "Invalid route field.",                     HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INVALID_STATE             = new ErrorInfo("route.invalidState",              "Invalid route state transition.",          HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ROUTE_HAS_BLOCKING_ISSUES = new ErrorInfo("route.hasBlockingIssues",         "Route has blocking validation issues.",    HttpStatus.BAD_REQUEST);
        private Route() {}
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────
    public static class Dispatch {
        public static final ErrorInfo ROUTE_STATUS_INVALID    = new ErrorInfo("dispatch.route.statusInvalid",    "Only PUBLISHED routes can be assigned.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo BUS_TIME_CONFLICT       = new ErrorInfo("dispatch.bus.timeConflict",       "Bus time window conflict.",              HttpStatus.CONFLICT);
        public static final ErrorInfo DRIVER_TIME_CONFLICT    = new ErrorInfo("dispatch.driver.timeConflict",    "Driver time window conflict.",           HttpStatus.CONFLICT);
        public static final ErrorInfo ATTENDANT_TIME_CONFLICT = new ErrorInfo("dispatch.attendant.timeConflict", "Attendant time window conflict.",        HttpStatus.CONFLICT);
        public static final ErrorInfo BUS_REQUIRED            = new ErrorInfo("dispatch.bus.required",            "Please select a bus in Route Builder first.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CANNOT_CHANGE_BUS       = new ErrorInfo("dispatch.bus.cannotChange",       "Cannot change the selected bus during staff assignment.", HttpStatus.BAD_REQUEST);
        private Dispatch() {}
    }

    // ── PlanningSession ───────────────────────────────────────────────────────
    public static class Session {
        public static final ErrorInfo CONFLICT                 = new ErrorInfo("session.conflict",               "Planning session already exists.",         HttpStatus.CONFLICT);
        public static final ErrorInfo SCHEDULE_MISMATCH       = new ErrorInfo("session.scheduleMismatch",        "Schedule does not belong to school.",      HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CANNOT_GENERATE         = new ErrorInfo("session.cannotGenerate",          "Cannot generate plan for this session.",   HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ALREADY_PUBLISHED       = new ErrorInfo("session.alreadyPublished",        "Session is already published.",            HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CANNOT_PUBLISH_CANCELLED= new ErrorInfo("session.cannotPublishCancelled",  "Cannot publish a cancelled session.",      HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NO_ROUTES               = new ErrorInfo("session.noRoutes",                "Session has no routes.",                   HttpStatus.BAD_REQUEST);
        public static final ErrorInfo UNASSIGNED_STUDENTS     = new ErrorInfo("session.unassignedStudents",      "Unassigned students remain.",              HttpStatus.BAD_REQUEST);
        public static final ErrorInfo BLOCKING_ISSUES         = new ErrorInfo("session.blockingIssues",          "Route has blocking issues.",               HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ALREADY_CANCELLED       = new ErrorInfo("session.alreadyCancelled",        "Session is already cancelled.",            HttpStatus.BAD_REQUEST);
        public static final ErrorInfo FROZEN                  = new ErrorInfo("session.frozen",                  "Session is closed for editing.",          HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ROUTE_NO_STUDENTS       = new ErrorInfo("session.routeNoStudents",         "Route has no assigned students.",          HttpStatus.BAD_REQUEST);
        private Session() {}
    }

    // ── Subscription ──────────────────────────────────────────────────────────
    public static class Subscription {
        public static final ErrorInfo OVERLAP         = new ErrorInfo("subscription.overlap",        "Active overlapping subscription exists.",  HttpStatus.CONFLICT);
        public static final ErrorInfo INVALID_STATE   = new ErrorInfo("subscription.invalidState",   "Invalid subscription state transition.",   HttpStatus.BAD_REQUEST);
        public static final ErrorInfo TARGET_REQUIRED = new ErrorInfo("subscription.targetRequired", "Target subscription is required.",        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INVALID_REQUEST = new ErrorInfo("subscription.invalidRequest", "Invalid subscription request.",           HttpStatus.BAD_REQUEST);
        private Subscription() {}
    }

    // ── Request (TransportRequest) ────────────────────────────────────────────
    public static class Request {
        public static final ErrorInfo CANNOT_EDIT             = new ErrorInfo("request.cannotEdit",             "Cannot edit request in current state.",    HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ONLY_SUBMITTED_APPROVED = new ErrorInfo("request.onlySubmittedApproved",  "Only SUBMITTED requests can be approved.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ONLY_SUBMITTED_REJECTED = new ErrorInfo("request.onlySubmittedRejected",  "Only SUBMITTED requests can be rejected.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INVALID_STATE           = new ErrorInfo("request.invalidState",           "Invalid request state transition.",        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INVALID_REQUEST         = new ErrorInfo("request.invalidRequest",         "Invalid transport request.",               HttpStatus.BAD_REQUEST);
        private Request() {}
    }

    // ── Trip ──────────────────────────────────────────────────────────────────
    public static class Trip {
        public static final ErrorInfo ROUTE_STATUS_INVALID    = new ErrorInfo("trip.routeStatus.invalid",    "Only ASSIGNED routes can create trips.",                                      HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NO_ASSIGNMENT           = new ErrorInfo("trip.noAssignment",           "Route must have an active assignment.",                                       HttpStatus.BAD_REQUEST);
        public static final ErrorInfo INVALID_STATE           = new ErrorInfo("trip.invalidState",           "Invalid trip state transition.",                                              HttpStatus.BAD_REQUEST);
        public static final ErrorInfo NO_STOPS                = new ErrorInfo("trip.noStops",               "Trip has no stops configured.",                                               HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STOP_ALREADY_DONE       = new ErrorInfo("trip.stop.alreadyDone",      "Stop is already departed or skipped.",                                        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STOP_NOT_ARRIVED        = new ErrorInfo("trip.stop.notArrived",       "Stop must be ARRIVED or BOARDING before it can be departed.",                 HttpStatus.BAD_REQUEST);
        public static final ErrorInfo STOP_NOT_ARRIVED_BOARDING = new ErrorInfo("trip.stop.notArrivedBoarding", "Stop must be ARRIVED to start boarding.",                                    HttpStatus.BAD_REQUEST);
        public static final ErrorInfo SKIP_REASON_REQUIRED    = new ErrorInfo("trip.skip.reasonRequired",   "A reason is required to skip a stop.",                                        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CANCEL_REASON_REQUIRED  = new ErrorInfo("trip.cancel.reasonRequired", "A reason is required to cancel a trip.",                                      HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ALREADY_CANCELLED       = new ErrorInfo("trip.alreadyCancelled",      "Trip is already cancelled.",                                                  HttpStatus.BAD_REQUEST);
        public static final ErrorInfo ALREADY_COMPLETED       = new ErrorInfo("trip.alreadyCompleted",      "Trip is already completed.",                                                  HttpStatus.BAD_REQUEST);
        public static final ErrorInfo UNPROCESSED_STUDENTS    = new ErrorInfo("trip.unprocessedStudents",   "Cannot complete trip: some students are still unprocessed (PLANNED).",        HttpStatus.BAD_REQUEST);
        public static final ErrorInfo CANNOT_SKIP_TERMINAL    = new ErrorInfo("trip.cannotSkipTerminal",    "Terminal depot/school stops cannot be skipped.",                              HttpStatus.BAD_REQUEST);
        private Trip() {}
    }

    // ── Export ────────────────────────────────────────────────────────────────
    public static class Export {
        public static final ErrorInfo HANDLER_NOT_FOUND  = new ErrorInfo("export.handlerNotFound",  "No export handler found for the requested export code.", HttpStatus.BAD_REQUEST);
        public static final ErrorInfo TEMPLATE_NOT_FOUND = new ErrorInfo("export.templateNotFound", "Export template file could not be found.",                HttpStatus.INTERNAL_SERVER_ERROR);
        public static final ErrorInfo TRACE_NOT_FOUND    = new ErrorInfo("export.traceNotFound",    "No routing calculation trace found. Please compute route first.", HttpStatus.NOT_FOUND);
        public static final ErrorInfo EXPORT_FAILED      = new ErrorInfo("export.failed",          "Failed to export data to Excel.",                         HttpStatus.INTERNAL_SERVER_ERROR);
        private Export() {}
    }

    // ── Security / Authorization ──────────────────────────────────────────────
    public static class Security {
        public static final ErrorInfo ACCESS_DENIED = new ErrorInfo("security.accessDenied", "Access denied.", HttpStatus.FORBIDDEN);
        public static final ErrorInfo FORBIDDEN_DATA_SCOPE = new ErrorInfo("security.forbiddenDataScope", "You do not have permission to access this data scope.", HttpStatus.FORBIDDEN);
        public static final ErrorInfo PARENT_PROFILE_NOT_FOUND = new ErrorInfo("security.parentProfileNotFound", "Parent profile not found for current user.", HttpStatus.NOT_FOUND);
        public static final ErrorInfo DRIVER_PROFILE_NOT_FOUND = new ErrorInfo("security.driverProfileNotFound", "Driver profile not found for current user.", HttpStatus.NOT_FOUND);
        public static final ErrorInfo ATTENDANT_PROFILE_NOT_FOUND = new ErrorInfo("security.attendantProfileNotFound", "Attendant profile not found for current user.", HttpStatus.NOT_FOUND);
        public static final ErrorInfo TRIP_NOT_ASSIGNED_TO_DRIVER = new ErrorInfo("security.tripNotAssignedToDriver", "Trip is not assigned to current driver.", HttpStatus.FORBIDDEN);
        public static final ErrorInfo TRIP_NOT_ASSIGNED_TO_ATTENDANT = new ErrorInfo("security.tripNotAssignedToAttendant", "Trip is not assigned to current attendant.", HttpStatus.FORBIDDEN);
        public static final ErrorInfo STUDENT_NOT_BELONG_TO_PARENT = new ErrorInfo("security.studentNotBelongToParent", "Student does not belong to current parent.", HttpStatus.FORBIDDEN);
        public static final ErrorInfo REQUEST_NOT_BELONG_TO_PARENT = new ErrorInfo("security.requestNotBelongToParent", "Request does not belong to current parent.", HttpStatus.FORBIDDEN);
        public static final ErrorInfo SUBSCRIPTION_NOT_BELONG_TO_PARENT = new ErrorInfo("security.subscriptionNotBelongToParent", "Subscription does not belong to current parent.", HttpStatus.FORBIDDEN);
        private Security() {}
    }

    private AppErrorCode() {}
}
