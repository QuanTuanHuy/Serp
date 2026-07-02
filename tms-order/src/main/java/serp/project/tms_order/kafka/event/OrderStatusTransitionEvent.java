/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusTransitionEvent {
    private Long tenantId;
    private InternalOrderStatusTransitionRequest request;
}
