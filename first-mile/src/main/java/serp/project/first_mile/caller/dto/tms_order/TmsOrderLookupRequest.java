/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller.dto.tms_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.OrderStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsOrderLookupRequest {
    private List<Long> orderIds;
    private List<String> orderCodes;
    private String destinationPostOfficeCode;
    private List<OrderStatus> statuses;
}
