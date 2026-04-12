/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.mapper;

import serp.project.first_mile.domain.Province;
import serp.project.first_mile.domain.Ward;
import serp.project.first_mile.dto.response.ProvinceResponse;
import serp.project.first_mile.dto.response.WardResponse;

public final class LocationMapper {
    private LocationMapper() {
    }

    public static ProvinceResponse toProvinceResponse(Province province) {
        return new ProvinceResponse(
                province.getProvinceCode(),
                province.getName(),
                province.getShortName(),
                province.getCode(),
                province.getPlaceType(),
                province.getCountryCode()
        );
    }

    public static WardResponse toWardResponse(Ward ward) {
        return new WardResponse(
                ward.getWardCode(),
                ward.getName(),
                ward.getProvinceCode()
        );
    }
}
