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
import serp.project.tms_billing_service.enums.VasRuleCode;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "vas_rules")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class VasRule extends AbstractAudit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private VasRuleCode code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "calculation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CalculationType calculationType;

    @Column(name = "rate_percent")
    private Double ratePercent;

    @Column(name = "fixed_amount")
    private Double fixedAmount;

    @Column(name = "min_amount")
    private Double minAmount;
}
