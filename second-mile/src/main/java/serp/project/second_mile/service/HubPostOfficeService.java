/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import java.util.List;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AssignHubPostOfficeRequest;
import serp.project.second_mile.dto.response.HubPostOfficeMappingResponse;

public interface HubPostOfficeService {

    PageResponse<HubPostOfficeMappingResponse> listPostOfficesForHub(long hubId, int page, int size);

    PageResponse<HubPostOfficeMappingResponse> listPostOfficeMappings(int page, int size);

    List<HubPostOfficeMappingResponse> assignPostOfficeToHub(long hubId, AssignHubPostOfficeRequest request);

    void removePostOfficeFromHub(long hubId, String postOfficeCode);
}
