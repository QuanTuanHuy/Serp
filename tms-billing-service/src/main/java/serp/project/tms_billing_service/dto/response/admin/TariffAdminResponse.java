/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.response.admin;

import lombok.Builder;
import lombok.Value;
import serp.project.tms_billing_service.enums.RouteType;

import java.time.LocalDate;

@Value
@Builder
public class TariffAdminResponse {
    Long id;
    String serviceCode;
    RouteType routeTypeCode;
    Double baseWeight;
    Double basePrice;
    Double stepWeight;
    Double stepPrice;
    LocalDate effectiveDate;
    LocalDate expirationDate;
}
