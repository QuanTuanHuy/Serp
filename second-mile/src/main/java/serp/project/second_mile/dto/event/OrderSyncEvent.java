package serp.project.second_mile.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.domain.Dimension;
import serp.project.second_mile.enums.OrderProductCategory;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.OrderType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSyncEvent {
    private String orderCode;
    private String customerOrderCode;
    private String originPostOfficeCode;
    private String destinationPostOfficeCode;
    private OrderStatus status;
    private Double totalWeight;
    private Dimension dimensions;
    private Double totalVolume;
    private OrderProductCategory orderProductCategory;
    private OrderType orderType;
    private String note;
}
