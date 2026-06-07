package serp.project.school_bus_service.enums;

import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

public enum RouteCalculationType {
    MATRIX,
    TIMELINE,
    MATRIX_AND_TIMELINE,
    GREEDY_GENERATION;

    public static RouteCalculationType parse(String value) {
        try {
            return valueOf(value == null ? "" : value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_ENUM_VALUE, "Invalid RouteCalculationType: " + value);
        }
    }
}
