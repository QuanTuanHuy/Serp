/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AddBagOrderRequest;
import serp.project.second_mile.dto.request.BagFilterRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.response.BagResponse;

public interface BagService {
    PageResponse<BagResponse> getBags(int page, int size, BagFilterRequest filterRequest);

    BagResponse getBagById(Long id);

    BagResponse createBag(CreateBagRequest request);

    BagResponse updateBag(Long id, UpdateBagRequest request);

    void deleteBag(Long id);

    BagResponse addOrderToBag(Long bagId, AddBagOrderRequest request);

    BagResponse removeOrderFromBag(Long bagId, String orderCode);
}
