/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.DeliveryManifestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "delivery_manifests")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryManifest extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manifest_code", nullable = false, length = 50)
    private String manifestCode;

    @Column(name = "post_office_code", nullable = false, length = 50)
    private String postOfficeCode;

    @Column(name = "courier_id")
    private Long courierId;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "vehicle_id", length = 50)
    private String vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private DeliveryManifestStatus status = DeliveryManifestStatus.CREATED;

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "planned_departure_at")
    private LocalDateTime plannedDepartureAt;

    @Column(name = "actual_departure_at")
    private LocalDateTime actualDepartureAt;

    @Column(name = "actual_return_at")
    private LocalDateTime actualReturnAt;

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(name = "delivered_count", nullable = false)
    @Builder.Default
    private Integer deliveredCount = 0;

    @Column(name = "failed_count", nullable = false)
    @Builder.Default
    private Integer failedCount = 0;

    @Column(name = "total_cod_amount", nullable = false)
    @Builder.Default
    private Long totalCodAmount = 0L;

    @Column(name = "collected_cod_amount", nullable = false)
    @Builder.Default
    private Long collectedCodAmount = 0L;

    @Column(name = "total_shipping_fee", nullable = false)
    @Builder.Default
    private Long totalShippingFee = 0L;

    @Column(name = "collected_shipping_fee", nullable = false)
    @Builder.Default
    private Long collectedShippingFee = 0L;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "route_geo_json", columnDefinition = "jsonb")
    private String routeGeoJson;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryManifestOrder> orders = new ArrayList<>();

    public void addOrder(DeliveryManifestOrder order) {
        if (orders == null) {
            orders = new ArrayList<>();
        }
        orders.add(order);
        order.setManifest(this);
    }

    public boolean isAllProcessed() {
        return (deliveredCount + failedCount) >= totalOrders;
    }
}
