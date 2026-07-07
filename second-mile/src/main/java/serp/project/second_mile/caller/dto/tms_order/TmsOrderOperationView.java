/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.caller.dto.tms_order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.domain.Dimension;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.OrderType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmsOrderOperationView {
    private Long id;
    private String orderCode;
    private String customerOrderCode;
    private OrderStatus status;
    private Boolean isConfirm;
    private String originPostOfficeCode;
    private String destinationPostOfficeCode;
    private Long currentHubId;
    private String currentHubCode;
    private Double totalWeight;
    private Double totalVolume;
    private Dimension dimensions;
    private OrderType orderType;
    private String note;
    private String createdBy;
    private LocalDateTime createdAt;
    private Long tenantId;
}
