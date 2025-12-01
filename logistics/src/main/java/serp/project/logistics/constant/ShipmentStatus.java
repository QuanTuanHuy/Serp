package serp.project.logistics.constant;

public enum ShipmentStatus {
    CREATED("CREATED"),
    IMPORTED("IMPORTED"),
    EXPORTED("EXPORTED")
    ;

    private final String value;

    ShipmentStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
