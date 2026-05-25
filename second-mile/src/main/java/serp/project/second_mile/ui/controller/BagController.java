/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import serp.project.second_mile.dto.request.BagFilterRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.BagService;

@RestController
@RequestMapping("/api/v1/bags")
@RequiredArgsConstructor
public class BagController {
    private final BagService bagService;
    private final MessageService messageService;

    @GetMapping
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
            @RequestParam(required = false) BagStatus status
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
                .build();

        return ApiResponse.<PageResponse<BagResponse>>builder()
                .message(messageService.getMessage("success.bags.list"))
                .result(bagService.getBags(page, size, filterRequest))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BagResponse> getBagById(@PathVariable Long id) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.detail"))
                .result(bagService.getBagById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<BagResponse> createBag(@Valid @RequestBody CreateBagRequest request) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.create"))
                .result(bagService.createBag(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
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
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<Void> deleteBag(@PathVariable Long id) {
        bagService.deleteBag(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.bags.delete"))
                .build();
    }

    @PostMapping("/{id}/orders")
    @PreAuthorize("hasRole('TMS_ADMIN')")
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
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<BagResponse> removeOrderFromBag(
            @PathVariable Long id,
            @PathVariable String orderCode
    ) {
        return ApiResponse.<BagResponse>builder()
                .message(messageService.getMessage("success.bags.order.remove"))
                .result(bagService.removeOrderFromBag(id, orderCode))
                .build();
    }
}
