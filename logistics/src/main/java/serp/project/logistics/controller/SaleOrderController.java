package serp.project.logistics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.entity.OrderEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.OrderService;
import serp.project.logistics.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics/api/v1/sale-order")
@Slf4j
public class SaleOrderController {

    private final OrderService orderService;
    private final AuthUtils authUtils;

    @GetMapping("/search/{orderId}")
    public ResponseEntity<GeneralResponse<OrderEntity>> getSaleOrderDetail(
                    @PathVariable String orderId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        var order = orderService.getSaleOrder(orderId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get sale order detail", order));
    }

}
