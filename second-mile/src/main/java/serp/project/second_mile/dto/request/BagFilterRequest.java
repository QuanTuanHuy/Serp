/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BagFilterRequest {
    private String keyword;
    private String bagCode;
    private Long originHubId;
    private BagDestinationType destinationType;
    private Long destinationHubId;
    private String destinationPostOfficeCode;
    private Long vehicleId;
    private BagStatus status;
}
