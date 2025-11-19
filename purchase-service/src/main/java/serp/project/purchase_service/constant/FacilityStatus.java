package serp.project.purchase_service.constant;

public enum FacilityStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),;

    private final String value;

    FacilityStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
