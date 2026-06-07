/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.CreateHubRequest;
import serp.project.second_mile.dto.request.HubImportDTO;
import serp.project.second_mile.dto.request.HubFilterRequest;
import serp.project.second_mile.dto.request.UpdateHubRequest;
import serp.project.second_mile.dto.response.HubResponse;
import serp.project.second_mile.dto.response.ImportHistoryResponse;
import serp.project.second_mile.dto.response.ValidateImportFileDTO;

public interface HubService {
    PageResponse<HubResponse> getHubs(int page, int size, HubFilterRequest filterRequest);

    HubResponse getHubById(Long id);

    byte[] exportTemplate();

    ValidateImportFileDTO<HubImportDTO> validateImportFile(MultipartFile file);

    ImportHistoryResponse importHubsAsync(MultipartFile file);

    HubResponse createHub(CreateHubRequest request);

    HubResponse updateHub(Long id, UpdateHubRequest request);

    HubResponse uploadImage(Long id, MultipartFile file);

    void deleteHub(Long id);
}
