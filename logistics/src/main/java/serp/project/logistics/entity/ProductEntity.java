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
@Builder
@Data
@Entity
@Table(name = "wms2_product")
public class ProductEntity {

    @Id
    private String id;

    private String name;

    private double weight;

    private double height;

    private String unit;

    @Column(name = "cost_price")
    private long costPrice;

    @Column(name = "whole_sale_price")
    private long wholeSalePrice;

    @Column(name = "retail_price")
    private long retailPrice;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "image_id")
    private String imageId;

    @Column(name = "extra_props")
    private String extraProps;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "vat_rate")
    private float vatRate;

    @Column(name = "sku_code")
    private String skuCode;

    @Column(name = "tenant_id")
    private Long tenantId;

}
