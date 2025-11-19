package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_customer")
public class CustomerEntity {

    @Id
    private Long id;

    private String name;

    @Column(name = "current_address_id")
    private String currentAddressId;

    private String statusId;

    private String phone;

    private String email;

    @Column(name = "tenant_id")
    private Long tenantId;

}
