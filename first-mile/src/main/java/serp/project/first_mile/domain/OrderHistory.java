package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.OrderStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@Table(name = "order_history")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistory extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "customer_order_code")
    private String customerOrderCode;

    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "description")
    private String description;

    @Column(name = "post_office_code")
    private String postOfficeCode;

    @Column(name = "staff_code")
    private String staffCode;

    @Column(name = "event_time")
    private LocalDateTime eventTime;
}
