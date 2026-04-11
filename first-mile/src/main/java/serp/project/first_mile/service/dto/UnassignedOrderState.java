package serp.project.first_mile.service.dto;

import lombok.Setter;

public final class UnassignedOrderState {
    private final PickupOrderNode order;
    private final boolean reinsertable;
    @Setter
    private String reason;

    public UnassignedOrderState(PickupOrderNode order, String reason, boolean reinsertable) {
        this.order = order;
        this.reason = reason;
        this.reinsertable = reinsertable;
    }

    public PickupOrderNode order() {
        return order;
    }

    public boolean reinsertable() {
        return reinsertable;
    }

    public String reason() {
        return reason;
    }

    public UnassignedOrderState copy() {
        return new UnassignedOrderState(order, reason, reinsertable);
    }
}
