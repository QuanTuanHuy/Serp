/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "chargeable_weight_configs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class ChargeableWeightConfig extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_code", nullable = false, unique = true)
    private String serviceCode;

    @Column(name = "min_dimension_cm", nullable = false)
    private Long minDimensionCm;

    @Column(name = "small_bulky_threshold_cm", nullable = false)
    private Long smallBulkyThresholdCm;

    @Column(name = "base_weight_gram", nullable = false)
    private Long baseWeightGram;

    @Column(name = "step_weight_gram", nullable = false)
    private Long stepWeightGram;

    @Column(name = "max_weight_gram", nullable = false)
    private Long maxWeightGram;

    @Column(name = "volumetric_divisor", nullable = false)
    private Double volumetricDivisor;
}
