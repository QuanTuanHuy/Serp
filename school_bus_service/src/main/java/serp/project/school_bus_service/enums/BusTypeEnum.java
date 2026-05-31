package serp.project.school_bus_service.enums;

import lombok.Getter;

@Getter
public enum BusTypeEnum {
    BUS_16_SEATS(16, "Xe 16 chỗ"),
    BUS_29_SEATS(29, "Xe 29 chỗ"),
    BUS_45_SEATS(45, "Xe 45 chỗ"),
    CUSTOM_BUS(0, "Xe tùy chỉnh");

    private final Integer value;
    private final String description;

    BusTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
