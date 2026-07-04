/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.tms_order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalPickupCandidateRequest {
    private String postOfficeCode;
    private List<OrderStatus> statuses;
    private LocalDateTime horizonStart;
    private LocalDateTime horizonEnd;
    private Integer limit;
}
