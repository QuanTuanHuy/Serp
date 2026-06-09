package serp.project.school_bus_service.enums;

import lombok.Getter;

@Getter
public enum BusTypeEnum {
    BUS_16_SEATS(16, "16-Seat Bus"),
    BUS_29_SEATS(29, "29-Seat Bus"),
    BUS_45_SEATS(45, "45-Seat Bus"),
    CUSTOM_BUS(0, "Custom Bus");

    private final Integer value;
    private final String description;

    BusTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
