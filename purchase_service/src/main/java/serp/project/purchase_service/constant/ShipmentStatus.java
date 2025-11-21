package serp.project.purchase_service.constant;

public enum ShipmentStatus {
    CREATED("CREATED"),
    READY("READY"),
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
