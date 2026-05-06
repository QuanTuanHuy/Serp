package serp.project.payment_service.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    PAYMENT_SUCCESS("PAYMENT_SUCCESS"),
    PAYMENT_FAILED("PAYMENT_FAILED"),
    REFUND_SUCCESS("REFUND_SUCCESS"),
    REFUND_FAILED("REFUND_FAILED");

    private final String defaultTopic;

    MessageType(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }
}
