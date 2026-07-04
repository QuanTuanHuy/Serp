package serp.project.school_bus_service.enums;

public enum TripStudentStatus {
    PLANNED,
    BOARDED,
    ABSENT,
    DROPPED_OFF,
    NO_SHOW,
    /** Stop or entire trip was skipped/cancelled before the student could board. Distinct from NO_SHOW (bus visited, student absent). */
    NOT_SERVED
}

