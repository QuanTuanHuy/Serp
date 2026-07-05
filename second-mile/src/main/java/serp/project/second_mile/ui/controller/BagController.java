/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AddBagOrderRequest;
import serp.project.second_mile.dto.request.AutoBaggingPlanRequest;
import serp.project.second_mile.dto.request.BagFilterRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.ReopenBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.request.ValidateBaggingRequest;
import serp.project.second_mile.dto.response.AutoBaggingPlanResponse;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.dto.response.BagSuggestionResponse;
import serp.project.second_mile.dto.response.BaggingKpiResponse;
import serp.project.second_mile.dto.response.BaggingValidationResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.BagService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bags")
@RequiredArgsConstructor
public class BagController {
    private final BagService bagService;
    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<PageResponse<BagResponse>> getBags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "bag_code", required = false) String bagCode,
            @RequestParam(name = "origin_hub_id", required = false) Long originHubId,
            @RequestParam(name = "destination_type", required = false) BagDestinationType destinationType,
            @RequestParam(name = "destination_hub_id", required = false) Long destinationHubId,
            @RequestParam(name = "destination_post_office_code", required = false) String destinationPostOfficeCode,
            @RequestParam(name = "vehicle_id", required = false) Long vehicleId,
            @RequestParam(required = false) BagStatus status,
            @RequestParam(name = "min_orders", required = false) Integer minOrders,
            @RequestParam(name = "max_orders", required = false) Integer maxOrders,
            @RequestParam(name = "min_weight", required = false) Double minWeight,
            @RequestParam(name = "max_weight", required = false) Double maxWeight,
            @RequestParam(name = "min_volume", required = false) Double minVolume,
            @RequestParam(name = "max_volume", required = false) Double maxVolume,
            @RequestParam(name = "sealed_from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sealedFrom,
            @RequestParam(name = "sealed_to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sealedTo,
            @RequestParam(name = "sort_by", required = false) String sortBy,
            @RequestParam(name = "sort_direction", required = false) String sortDirection
    ) {
        BagFilterRequest filterRequest = BagFilterRequest.builder()
                .keyword(keyword)
                .bagCode(bagCode)
                .originHubId(originHubId)
                .destinationType(destinationType)
                .destinationHubId(destinationHubId)
                .destinationPostOfficeCode(destinationPostOfficeCode)
                .vehicleId(vehicleId)
                .status(status)
                .minOrders(minOrders)
                .maxOrders(maxOrders)
                .minWeight(minWeight)
                .maxWeight(maxWeight)
                .minVolume(minVolume)
                .maxVolume(maxVolume)
                .sealedFrom(sealedFrom)
                .sealedTo(sealedTo)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        return ApiResponse.<PageResponse<BagResponse>>builder()
                .message(messageService.getMessage("success.bags.list"))
                .result(bagService.getBags(page, size, filterRequest))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BagResponse> getBagById(@PathVariable Long id) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.detail"))
                .result(bagService.getBagById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagResponse> createBag(@Valid @RequestBody CreateBagRequest request) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.create"))
                .result(bagService.createBag(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagResponse> updateBag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBagRequest request
    ) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.update"))
                .result(bagService.updateBag(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<Void> deleteBag(@PathVariable Long id) {
        bagService.deleteBag(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.bags.delete"))
                .build();
    }

    @PostMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BagResponse> addOrderToBag(
            @PathVariable Long id,
            @Valid @RequestBody AddBagOrderRequest request
    ) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.order.add"))
                .result(bagService.addOrderToBag(id, request))
                .build();
    }

    @DeleteMapping("/{id}/orders/{orderCode}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BagResponse> removeOrderFromBag(
            @PathVariable Long id,
            @PathVariable String orderCode
    ) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.order.remove"))
                .result(bagService.removeOrderFromBag(id, orderCode))
                .build();
    }

    @PostMapping("/{id}/seal")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagResponse> sealBag(@PathVariable Long id) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.seal"))
                .result(bagService.sealBag(id))
                .build();
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagResponse> reopenBag(
            @PathVariable Long id,
            @Valid @RequestBody ReopenBagRequest request
    ) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.reopen"))
                .result(bagService.reopenBag(id, request))
                .build();
    }

    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<List<BagSuggestionResponse>> suggestBags(
            @RequestParam(name = "order_code") String orderCode,
            @RequestParam(name = "origin_hub_id", required = false) Long originHubId
    ) {
        return ApiResponse.<List<BagSuggestionResponse>>builder()
                .message(messageService.getMessage("success.bags.suggestions"))
                .result(bagService.suggestBags(orderCode, originHubId))
                .build();
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BaggingValidationResponse> validateBagging(@Valid @RequestBody ValidateBaggingRequest request) {
        return ApiResponse.<BaggingValidationResponse>builder()
                .message(messageService.getMessage("success.bags.validate"))
                .result(bagService.validateBagging(request))
                .build();
    }

    @PostMapping("/auto-plan")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<AutoBaggingPlanResponse> autoPlanBags(@Valid @RequestBody AutoBaggingPlanRequest request) {
        return ApiResponse.<AutoBaggingPlanResponse>builder()
                .message(messageService.getMessage("success.bags.auto_plan"))
                .result(bagService.autoPlanBags(request))
                .build();
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BaggingKpiResponse> getBaggingKpis(
            @RequestParam(name = "origin_hub_id") Long originHubId,
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ApiResponse.<BaggingKpiResponse>builder()
                .message(messageService.getMessage("success.bags.kpis"))
                .result(bagService.getBaggingKpi(originHubId, from, to))
                .build();
    }
}
