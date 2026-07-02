/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsOrderStatusTransitionEvent {
    private Long tenantId;
    private TmsOrderStatusTransitionRequest request;
}
