/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AddBagOrderRequest;
import serp.project.second_mile.dto.request.AutoBaggingPlanRequest;
import serp.project.second_mile.dto.request.BagFilterRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.ReopenBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.request.ValidateBaggingRequest;
import serp.project.second_mile.dto.response.AutoBaggingPlanResponse;
import serp.project.second_mile.dto.response.BagSuggestionResponse;
import serp.project.second_mile.dto.response.BaggingKpiResponse;
import serp.project.second_mile.dto.response.BaggingValidationResponse;
import serp.project.second_mile.dto.response.BagResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface BagService {
    PageResponse<BagResponse> getBags(int page, int size, BagFilterRequest filterRequest);

    BagResponse getBagById(Long id);

    BagResponse createBag(CreateBagRequest request);

    BagResponse updateBag(Long id, UpdateBagRequest request);

    void deleteBag(Long id);

    BagResponse addOrderToBag(Long bagId, AddBagOrderRequest request);

    BagResponse removeOrderFromBag(Long bagId, String orderCode);

    BagResponse sealBag(Long bagId);

    BagResponse reopenBag(Long bagId, ReopenBagRequest request);

    List<BagSuggestionResponse> suggestBags(String orderCode, Long originHubId);

    BaggingValidationResponse validateBagging(ValidateBaggingRequest request);

    AutoBaggingPlanResponse autoPlanBags(AutoBaggingPlanRequest request);

    BaggingKpiResponse getBaggingKpi(Long originHubId, LocalDateTime from, LocalDateTime to);
}
