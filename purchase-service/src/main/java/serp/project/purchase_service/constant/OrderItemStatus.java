package serp.project.purchase_service.constant;

public enum OrderItemStatus {
    CREATED("CREATED"),
    DELIVERED("DELIVERED"),
    ;

    private final String value;

    OrderItemStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
