package serp.project.payment_service.kafka.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSuccessEvent {
    String email;
    String appUser;
    String title;
    String description;
    String appTransId;
    Long amount;
    LocalDateTime paidAt;
}
