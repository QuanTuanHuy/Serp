/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.response.GeocodeAddressResponse;

public interface GeocodeService {
    GeocodeAddressResponse geocodeAddress(String address);
}