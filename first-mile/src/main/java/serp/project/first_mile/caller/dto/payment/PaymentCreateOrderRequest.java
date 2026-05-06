/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateOrderRequest {
    private String appUser;
    private Long amount;
    private String description;
    private List<Item> items;
    private String title;
    private String email;
    private Long tenantId;
    private Long actorId;
    private Long userId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String itemId;
        private String itemName;
        private Long itemPrice;
        private Integer itemQuantity;
    }
}
