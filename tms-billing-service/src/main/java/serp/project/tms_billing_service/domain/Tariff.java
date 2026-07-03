/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.tms_billing_service.enums.RouteType;

import java.time.LocalDate;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "tariffs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Tariff extends AbstractAudit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_code", nullable = false)
    private String serviceCode;

    @Column(name = "route_type_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private RouteType routeTypeCode;

    @Column(name = "base_weight", nullable = false)
    private Double baseWeight;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "step_weight", nullable = false)
    private Double stepWeight;

    @Column(name = "step_price", nullable = false)
    private Double stepPrice;

    @Column(name = "effective_date", nullable = false)
    LocalDate effectiveDate;

    @Column(name = "expiration_date")
    LocalDate expirationDate;
}
