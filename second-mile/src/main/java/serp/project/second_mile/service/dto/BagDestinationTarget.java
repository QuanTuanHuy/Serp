/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.dto;

import serp.project.second_mile.enums.BagDestinationType;

public record BagDestinationTarget(
        BagDestinationType destinationType,
        Long destinationHubId,
        String destinationPostOfficeCode
) {
}
