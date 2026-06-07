package serp.project.payment_service.kafka.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundSuccessEvent {
    String email;
    String appUser;
    String title;
    String description;
    String mRefundId;
    String zpTransId;
    Long amount;
    Long refundFeeAmount;
    String refundReason;
    LocalDateTime refundedAt;
}