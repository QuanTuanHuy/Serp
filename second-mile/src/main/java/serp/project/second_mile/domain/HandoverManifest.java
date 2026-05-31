/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

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
import serp.project.second_mile.enums.HandoverManifestStatus;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "handover_manifests")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class HandoverManifest extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manifest_code", nullable = false, length = 100)
    private String manifestCode;

    @Column(name = "origin_post_office_code", nullable = false, length = 255)
    private String originPostOfficeCode;

    @Column(name = "target_hub_id", nullable = false)
    private Long targetHubId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "route_id")
    private Long routeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private HandoverManifestStatus status;
}
