package serp.project.purchase_service.constant;
public enum OrderStatus {
    CREATED("CREATED"),
    APPROVED("APPROVED"),
    CANCELLED("CANCELLED"),
    READY_FOR_DELIVERY("READY_FOR_DELIVERY"),
    FULLY_DELIVERED("FULLY_DELIVERED")
    ;
    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
