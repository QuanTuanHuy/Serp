/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import lombok.Builder;
import lombok.Getter;
import serp.project.tms_order.enums.OrderType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DashboardFilterRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String timezone;
    private String granularity;
    private Long hubId;
    private Long postOfficeId;
    private String postOfficeCode;
    private List<String> postOfficeCodes;
    private OrderType serviceType;
}
