package serp.project.tms_payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @NotBlank(message = "Item ID không được để trống")
    @JsonProperty("itemid")
    private String itemId;
    
    @NotBlank(message = "Item name không được để trống")
    @JsonProperty("itemname")
    private String itemName;
    
    @NotNull(message = "Item price không được để trống")
    @Min(value = 0, message = "Item price phải >= 0")
    @JsonProperty("itemprice")
    private Long itemPrice;
    
    @NotNull(message = "Item quantity không được để trống")
    @Min(value = 1, message = "Item quantity phải >= 1")
    @JsonProperty("itemquantity")
    private Integer itemQuantity;
}
