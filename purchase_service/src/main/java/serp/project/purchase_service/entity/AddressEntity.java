package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "wms2_address")
public class AddressEntity {

    @Id
    private String id;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "address_type")
    private String addressType;

    private float latitude;

    private float longitude;

    @Column(name = "is_default")
    private boolean isDefault;

    @Column(name = "full_address")
    private String fullAddress;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

}
