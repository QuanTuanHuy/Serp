/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreatePostOfficeRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeRequest;
import serp.project.first_mile.dto.response.PostOfficeGeocodeBatchResponse;
import serp.project.first_mile.dto.response.PostOfficeResponse;

public interface PostOfficeService {
    PageResponse<PostOfficeResponse> getPostOffices(int page, int size, String keyword);

    PostOfficeResponse getPostOfficeById(Long id);

    PostOfficeResponse createPostOffice(CreatePostOfficeRequest request);

    PostOfficeResponse updatePostOffice(Long id, UpdatePostOfficeRequest request);

    void deletePostOffice(Long id);

    PostOfficeResponse updatePostOfficeLocationByGeocode(Long id);

    PostOfficeGeocodeBatchResponse updatePostOfficesWithNullLocationByGeocode(int batch);
}
