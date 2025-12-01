package serp.project.logistics.entity;

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
@Data
@Builder
@Entity
@Table(name = "wms2_facility")
public class FacilityEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "current_address_id")
    private String currentAddressId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "is_default")
    private boolean isDefault;

    private String phone;

    @Column(name = "postal_code")
    private String postalCode;

    private float length;
    private float width;
    private float height;

    @Column(name = "tenant_id")
    private Long tenantId;

}
