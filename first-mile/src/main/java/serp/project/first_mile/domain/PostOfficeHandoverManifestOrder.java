/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.OrderStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(
        name = "post_office_handover_manifest_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_po_handover_manifest_order",
                columnNames = {"manifest_id", "order_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class PostOfficeHandoverManifestOrder extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manifest_id", nullable = false)
    private PostOfficeHandoverManifest manifest;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "customer_order_code")
    private String customerOrderCode;

    @Column(name = "last_known_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus lastKnownStatus;

    @Column(name = "scan_out_time")
    private LocalDateTime scanOutTime;

    @Column(name = "scan_in_time")
    private LocalDateTime scanInTime;
}
