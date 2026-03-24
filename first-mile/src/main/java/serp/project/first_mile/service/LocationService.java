/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.response.ProvinceResponse;
import serp.project.first_mile.dto.response.WardResponse;

public interface LocationService {
    PageResponse<ProvinceResponse> getProvinces(int page, int size);

    PageResponse<WardResponse> getWardsByProvinceCode(String provinceCode, int page, int size);
}
