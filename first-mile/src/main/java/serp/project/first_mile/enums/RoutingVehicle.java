package serp.project.first_mile.enums;

import java.util.Locale;

public enum RoutingVehicle {
    CAR("car"),
    BIKE("bike"),
    TAXI("taxi"),
    TRUCK("truck"),
    HD("hd");

    private final String apiValue;

    RoutingVehicle(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public static RoutingVehicle fromValue(String value) {
        if (value == null || value.isBlank()) {
            return CAR;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return RoutingVehicle.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return CAR;
        }
    }
}
