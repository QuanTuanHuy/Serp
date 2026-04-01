/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

public record GeocodeAddressResponse(
        String address,
        Double latitude,
        Double longitude
) {
}