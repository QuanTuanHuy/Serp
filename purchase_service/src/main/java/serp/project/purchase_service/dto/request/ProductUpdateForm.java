package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;
import serp.project.purchase_service.constant.ProductStatus;
import serp.project.purchase_service.validator.EnumValidator;

@Data
public class ProductUpdateForm {

    private String name;

    @Min(value = 0, message = "Weight must be non-negative")
    private double weight;

    @Min(value = 0, message = "Height must be non-negative")
    private double height;

    private String unit;

    @Min(value = 0, message = "Cost price must be non-negative")
    private long costPrice;

    @Min(value = 0, message = "Wholesale price must be non-negative")
    private long wholeSalePrice;

    @Min(value = 0, message = "Retail price must be non-negative")
    private long retailPrice;

    @EnumValidator(enumClass = ProductStatus.class)
    private String statusId;

    private String imageId;
    private String extraProps;

    @Min(value = 0, message = "VAT rate must be non-negative")
    private float vatRate;

    private String skuCode;

}
