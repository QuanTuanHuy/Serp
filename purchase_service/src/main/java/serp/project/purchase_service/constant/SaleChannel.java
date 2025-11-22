package serp.project.purchase_service.constant;

public enum SaleChannel {

    PARTNER("PARTNER"),;

    private final String value;

    SaleChannel(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
