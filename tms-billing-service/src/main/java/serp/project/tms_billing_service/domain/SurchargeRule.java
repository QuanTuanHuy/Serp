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
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;

import java.time.LocalDate;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "surcharge_rules")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class SurchargeRule extends AbstractAudit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private SurchargeRuleEnum code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "calculation_type")
    @Enumerated(EnumType.STRING)
    private CalculationType calculationType;

    @Column(name = "rate_percent")
    private Double ratePercent;

    @Column(name = "fixed_amount")
    private Double fixedAmount;

    @Column(name = "min_amount")
    private Double minAmount;

    @Column(name = "base_weight")
    private Double baseWeight;

    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "step_weight")
    private Double stepWeight;

    @Column(name = "step_price")
    private Double stepPrice;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;
}
