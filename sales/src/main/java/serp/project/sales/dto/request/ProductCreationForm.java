package serp.project.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.sales.constant.ProductStatus;
import serp.project.sales.validator.EnumValidator;

@Data
public class ProductCreationForm {

    @NotBlank(message = "Product name must not be blank")
    private String name;

    @Min(value = 0, message = "Weight must be non-negative")
    private double weight;

    @Min(value = 0, message = "Height must be non-negative")
    private double height;

    @NotBlank(message = "Unit must not be blank")
    private String unit;

    @Min(value = 0, message = "Cost price must be non-negative")
    private long costPrice;

    @Min(value = 0, message = "Wholesale price must be non-negative")
    private long wholeSalePrice;

    @Min(value = 0, message = "Retail price must be non-negative")
    private long retailPrice;

    @NotBlank(message = "Category ID must not be blank")
    private String categoryId;

    @NotNull(message = "Status ID must not be blank")
    @EnumValidator(enumClass = ProductStatus.class)
    private String statusId;

    private String imageId;
    private String extraProps;

    @Min(value = 0, message = "VAT rate must be non-negative")
    private float vatRate;

    @NotBlank(message = "SKU code must not be blank")
    private String skuCode;

}
