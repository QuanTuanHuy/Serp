package serp.project.school_bus_service.enums;

import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

public enum RequestType {
    NEW_SERVICE,
    CHANGE_SERVICE,
    PAUSE_SERVICE,
    RESUME_SERVICE,
    STOP_SERVICE,
    RENEW_SERVICE;

    public static RequestType parse(String value) {
        try { return valueOf(value == null ? "" : value.toUpperCase()); }
        catch (IllegalArgumentException e) {
            throw new AppException(AppErrorCode.INVALID_ENUM_VALUE, "Invalid requestType: " + value
                    + ". Allowed: " + java.util.Arrays.toString(values()));
        }
    }
}
