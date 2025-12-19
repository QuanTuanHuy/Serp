package serp.project.sales.entity;

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
import org.springframework.util.StringUtils;
import serp.project.sales.dto.request.ProductCreationForm;
import serp.project.sales.dto.request.ProductUpdateForm;
import serp.project.sales.util.IdUtils;

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
        this.id = IdUtils.generateProductId();
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
        if (StringUtils.hasText(form.getName()))
            this.setName(form.getName());
        if (form.getWeight() != 0)
            this.setWeight(form.getWeight());
        if (form.getHeight() != 0)
            this.setHeight(form.getHeight());
        if (StringUtils.hasText(form.getUnit()))
            this.setUnit(form.getUnit());
        if (form.getCostPrice() != 0)
            this.setCostPrice(form.getCostPrice());
        if (form.getWholeSalePrice() != 0)
            this.setWholeSalePrice(form.getWholeSalePrice());
        if (form.getRetailPrice() != 0)
            this.setRetailPrice(form.getRetailPrice());
        if (StringUtils.hasText(form.getStatusId()))
            this.setStatusId(form.getStatusId());
        if (StringUtils.hasText(form.getImageId()))
            this.setImageId(form.getImageId());
        if (StringUtils.hasText(form.getExtraProps()))
            this.setExtraProps(form.getExtraProps());

        this.setVatRate(form.getVatRate());

        if (StringUtils.hasText(form.getSkuCode()))
            this.setSkuCode(form.getSkuCode());
    }

}
