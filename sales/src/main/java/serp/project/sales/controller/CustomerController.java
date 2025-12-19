package serp.project.sales.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.sales.dto.request.CustomerCreationForm;
import serp.project.sales.dto.request.CustomerUpdateForm;
import serp.project.sales.dto.response.GeneralResponse;
import serp.project.sales.dto.response.PageResponse;
import serp.project.sales.entity.AddressEntity;
import serp.project.sales.entity.CustomerEntity;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.service.AddressService;
import serp.project.sales.service.CustomerService;
import serp.project.sales.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("sales/api/v1/customer")
@Validated
@Slf4j
public class CustomerController {

        private final CustomerService customerService;
        private final AuthUtils authUtils;
        private final AddressService addressService;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createCustomer(@Valid @RequestBody CustomerCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CustomerController] Creating customer {} for tenantId {}", form.getName(), tenantId);
                customerService.createCustomer(form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Customer created successfully"));
        }

        @PatchMapping("/update/{customerId}")
        public ResponseEntity<GeneralResponse<?>> updateCustomer(@Valid @RequestBody CustomerUpdateForm form,
                        @PathVariable("customerId") String customerId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CustomerController] Updating customer {} with ID {} for tenantId {}", form.getName(),
                                customerId,
                                tenantId);
                customerService.updateCustomer(customerId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Customer updated successfully"));
        }

        @DeleteMapping("/delete/{customerId}")
        public ResponseEntity<GeneralResponse<?>> deleteCustomer(@PathVariable("customerId") String customerId) {
                throw new AppException(AppErrorCode.UNIMPLEMENTED);
        }

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

        @GetMapping("/search/{customerId}")
        public ResponseEntity<GeneralResponse<CustomerEntity>> getDetailCustomer(
                        @PathVariable("customerId") String customerId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CustomerController] Getting detail customer with ID {} for tenantId {}", customerId,
                                tenantId);
                CustomerEntity customer = customerService.getCustomer(customerId, tenantId);
                if (customer == null) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }
                AddressEntity address = addressService.findByEntityId(customerId, tenantId).stream()
                                .filter(AddressEntity::isDefault).findFirst().orElse(null);
                customer.setAddress(address);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get customer detail", customer));
        }

}
