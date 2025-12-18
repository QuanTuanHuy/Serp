package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.purchase_service.dto.request.ProductCreationForm;
import serp.project.purchase_service.dto.request.ProductUpdateForm;
import serp.project.purchase_service.util.IdUtils;

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

    public ProductEntity(ProductCreationForm form, Long tenantId) {
        String productId = IdUtils.generateProductId();
        this.id = productId;
        this.name = form.getName();
        this.weight = form.getWeight();
        this.height = form.getHeight();
        this.unit = form.getUnit();
        this.costPrice = form.getCostPrice();
        this.wholeSalePrice = form.getWholeSalePrice();
        this.retailPrice = form.getRetailPrice();
        this.categoryId = form.getCategoryId();
        this.statusId = form.getStatusId();
        this.imageId = form.getImageId();
        this.extraProps = form.getExtraProps();
        this.vatRate = form.getVatRate(); // Default: 0.0f
        this.skuCode = form.getSkuCode();
        this.tenantId = tenantId;
    }

    public void update(ProductUpdateForm form) {
        if (form.getName() != null)
            this.setName(form.getName());
        if (form.getWeight() != 0)
            this.setWeight(form.getWeight());
        if (form.getHeight() != 0)
            this.setHeight(form.getHeight());
        if (form.getUnit() != null)
            this.setUnit(form.getUnit());
        if (form.getCostPrice() != 0)
            this.setCostPrice(form.getCostPrice());
        if (form.getWholeSalePrice() != 0)
            this.setWholeSalePrice(form.getWholeSalePrice());
        if (form.getRetailPrice() != 0)
            this.setRetailPrice(form.getRetailPrice());
        if (form.getStatusId() != null)
            this.setStatusId(form.getStatusId());
        if (form.getImageId() != null)
            this.setImageId(form.getImageId());
        if (form.getExtraProps() != null)
            this.setExtraProps(form.getExtraProps());

        this.setVatRate(form.getVatRate());

        if (form.getSkuCode() != null)
            this.setSkuCode(form.getSkuCode());
    }

}
