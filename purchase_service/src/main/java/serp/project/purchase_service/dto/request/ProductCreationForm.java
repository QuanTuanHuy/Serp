package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductCreationForm {

    @NotBlank(message = "name is mandatory")
    private String name;

    private double weight;
    private double height;

    @NotBlank(message = "unit is mandatory")
    private String unit;

    @NotBlank(message = "cost price is mandatory")
    private long costPrice;

    @NotBlank(message = "whole sale price is mandatory")
    private long wholeSalePrice;

    private long retailPrice;

    @NotBlank(message = "categoryId is mandatory")
    private String categoryId;

    private String statusId;

    private String imageId;
    private String extraProps;
    private float vatRate;

    @NotBlank(message = "skuCode is mandatory")
    private String skuCode;

}
