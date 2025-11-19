package serp.project.purchase_service.constant;

public enum InvoiceStatus {
    CREATED("CREATED"),
    ;

    private final String value;

    InvoiceStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
