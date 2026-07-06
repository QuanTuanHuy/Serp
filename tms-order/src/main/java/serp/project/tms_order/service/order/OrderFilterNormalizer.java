/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.order;

import org.springframework.stereotype.Component;
import serp.project.tms_order.dto.request.OrderFilterRequest;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OrderFilterNormalizer {

    public OrderFilterRequest normalize(OrderFilterRequest filterRequest) {
        if (filterRequest == null) {
            return OrderFilterRequest.builder().build();
        }

        return OrderFilterRequest.builder()
                .keyword(OrderTextUtils.normalizeText(filterRequest.getKeyword()))
                .orderCode(OrderTextUtils.normalizeText(filterRequest.getOrderCode()))
                .customerOrderCode(OrderTextUtils.normalizeText(filterRequest.getCustomerOrderCode()))
                .senderKeyword(OrderTextUtils.normalizeText(filterRequest.getSenderKeyword()))
                .senderPhone(OrderTextUtils.normalizeText(filterRequest.getSenderPhone()))
                .receiverKeyword(OrderTextUtils.normalizeText(filterRequest.getReceiverKeyword()))
                .receiverPhone(OrderTextUtils.normalizeText(filterRequest.getReceiverPhone()))
                .originPostOfficeCode(OrderTextUtils.normalizeText(filterRequest.getOriginPostOfficeCode()))
                .originPostOfficeCodes(normalizeTextList(filterRequest.getOriginPostOfficeCodes()))
                .destinationPostOfficeCode(OrderTextUtils.normalizeText(filterRequest.getDestinationPostOfficeCode()))
                .status(filterRequest.getStatus())
                .statuses(normalizeOrderStatuses(filterRequest))
                .pickupMethod(filterRequest.getPickupMethod())
                .isConfirm(filterRequest.getIsConfirm())
                .createdFrom(filterRequest.getCreatedFrom())
                .createdTo(filterRequest.getCreatedTo())
                .pickupFrom(filterRequest.getPickupFrom())
                .pickupTo(filterRequest.getPickupTo())
                .updatedFrom(filterRequest.getUpdatedFrom())
                .updatedTo(filterRequest.getUpdatedTo())
                .sortBy(OrderTextUtils.normalizeText(filterRequest.getSortBy()))
                .sortDirection(OrderTextUtils.normalizeText(filterRequest.getSortDirection()))
                .build();
    }

    public void validateRanges(OrderFilterRequest filterRequest) {
        if (filterRequest.getCreatedFrom() != null
                && filterRequest.getCreatedTo() != null
                && filterRequest.getCreatedFrom().isAfter(filterRequest.getCreatedTo())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (filterRequest.getPickupFrom() != null
                && filterRequest.getPickupTo() != null
                && filterRequest.getPickupFrom().isAfter(filterRequest.getPickupTo())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (filterRequest.getUpdatedFrom() != null
                && filterRequest.getUpdatedTo() != null
                && filterRequest.getUpdatedFrom().isAfter(filterRequest.getUpdatedTo())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private List<OrderStatus> normalizeOrderStatuses(OrderFilterRequest filterRequest) {
        List<OrderStatus> statuses = new ArrayList<>();
        if (filterRequest.getStatuses() != null) {
            for (OrderStatus status : filterRequest.getStatuses()) {
                if (status != null && !statuses.contains(status)) {
                    statuses.add(status);
                }
            }
        }
        if (filterRequest.getStatus() != null && !statuses.contains(filterRequest.getStatus())) {
            statuses.add(filterRequest.getStatus());
        }
        return statuses;
    }

    private List<String> normalizeTextList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> normalizedValues = new ArrayList<>();
        for (String value : values) {
            String normalizedValue = OrderTextUtils.normalizeText(value);
            if (normalizedValue != null) {
                normalizedValue = normalizedValue.toLowerCase(Locale.ROOT);
                if (!normalizedValues.contains(normalizedValue)) {
                    normalizedValues.add(normalizedValue);
                }
            }
        }
        return normalizedValues;
    }
}
