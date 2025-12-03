package serp.project.logistics.dto.request;

import lombok.Data;

@Data
public class ProductCreationForm {

    private String name;
    private double weight;
    private double height;
    private String unit;
    private long costPrice;
    private long wholeSalePrice;
    private long retailPrice;
    private String categoryId;
    private String statusId;
    private String imageId;
    private String extraProps;
    private float vatRate;
    private String skuCode;

}
