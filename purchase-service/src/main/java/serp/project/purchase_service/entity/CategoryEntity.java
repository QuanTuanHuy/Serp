package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_product_category")
public class CategoryEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "tenant_id")
    private Long tenantId;
}
