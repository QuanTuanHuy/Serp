/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.first_mile.caller.GeocodeCaller;
import serp.project.first_mile.dto.response.GeocodeAddressResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.service.GeocodeService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodeServiceImpl implements GeocodeService {

    private final GeocodeCaller geocodeCaller;

    @Override
    public GeocodeAddressResponse geocodeAddress(String address) {
        String sanitizedAddress = Optional.ofNullable(address)
                .map(String::trim)
                .orElse("");

        GeocodeCaller.GeoPoint geoPoint = geocodeCaller.searchFirst(sanitizedAddress)
                .orElseThrow(() -> {
                    log.warn("No geocode result for address={}", sanitizedAddress);
                    return new AppException(ErrorCode.GEOCODE_NOT_FOUND);
                });

        return new GeocodeAddressResponse(
                sanitizedAddress,
                geoPoint.latitude(),
                geoPoint.longitude()
        );
    }
}