package serp.project.school_bus_service.enums;

public enum AttendanceEventType {
    BOARDED,
    DROPPED_OFF,
    ABSENT,
    NO_SHOW,
    /** Stop was skipped or trip was cancelled before the student could be served. System-generated. */
    NOT_SERVED
}

