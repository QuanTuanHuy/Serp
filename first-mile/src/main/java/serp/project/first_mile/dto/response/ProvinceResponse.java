/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

public record ProvinceResponse(
        String provinceCode,
        String name,
        String shortName,
        String code,
        String placeType,
        String countryCode
) {
}
