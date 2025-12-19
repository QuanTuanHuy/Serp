package serp.project.logistics.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.entity.CustomerEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.CustomerService;
import serp.project.logistics.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("logistics/api/v1/customer")
@Validated
@Slf4j
public class CustomerController {

        private final CustomerService customerService;
        private final AuthUtils authUtils;

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<CustomerEntity>>> getCustomers(
                        @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String statusId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CustomerController] Getting customers of page {}/{} for tenantId {}", page, size, tenantId);
                Page<CustomerEntity> customers = customerService.findCustomers(
                                query,
                                statusId,
                                tenantId,
                                page,
                                size,
                                sortBy,
                                sortDirection);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get list of customers at page " + page,
                                PageResponse.of(customers)));
        }

}
