package serp.project.school_bus_service.enums;

import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

public enum TripOption {
    MORNING,
    AFTERNOON,
    ROUND_TRIP;

    public static TripOption parse(String value) {
        try { return valueOf(value == null ? "" : value.toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_ENUM_VALUE, "Invalid tripOption: " + value);
        }
    }

    /** Returns null instead of throwing when value is blank. */
    public static TripOption parseNullable(String value) {
        if (value == null || value.isBlank()) return null;
        return parse(value);
    }
}
