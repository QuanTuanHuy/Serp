/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteFilterRequest {
    private String keyword;
    private String routeCode;
    private Long originHubId;
    private RouteDestinationType destinationType;
    private Long destinationHubId;
    private String destinationPostOfficeCode;
    private Long vehicleId;
    private RouteStatus status;
}
