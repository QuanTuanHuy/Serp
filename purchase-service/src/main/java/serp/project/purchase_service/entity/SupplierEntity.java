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
@Table(name = "wms2_supplier")
public class SupplierEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "current_address_id")
    private String currentAddressId;

    private String email;

    private String phone;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "tenant_id")
    private Long tenantId;

}
