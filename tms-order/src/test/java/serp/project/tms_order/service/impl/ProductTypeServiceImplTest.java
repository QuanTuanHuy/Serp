/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.request.CreateProductTypeRequest;
import serp.project.tms_order.dto.response.ProductTypeResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.repository.ProductTypeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductTypeServiceImplTest {

    private static final Long TENANT_ID = 9L;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @InjectMocks
    private ProductTypeServiceImpl productTypeService;

    @Test
    void createProductTypeTrimsFieldsDefaultsActiveAndSetsTenant() {
        CreateProductTypeRequest request = new CreateProductTypeRequest(" BOX ", " Box ", null);

        when(productTypeRepository.existsByCodeIgnoreCaseAndTenantId("BOX", TENANT_ID))
                .thenReturn(false);
        when(productTypeRepository.save(any(ProductType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductTypeResponse response = productTypeService.createProductType(request, TENANT_ID);

        ArgumentCaptor<ProductType> captor = ArgumentCaptor.forClass(ProductType.class);
        verify(productTypeRepository).save(captor.capture());
        ProductType savedProductType = captor.getValue();

        assertEquals("BOX", savedProductType.getCode());
        assertEquals("Box", savedProductType.getName());
        assertTrue(savedProductType.getIsActive());
        assertEquals(TENANT_ID, savedProductType.getTenantId());
        assertEquals("BOX", response.code());
        assertEquals("Box", response.name());
        assertTrue(response.isActive());
        assertEquals(TENANT_ID, response.tenantId());
    }

    @Test
    void createProductTypeRejectsDuplicatedCodeInTenant() {
        CreateProductTypeRequest request = new CreateProductTypeRequest("BOX", "Box", true);

        when(productTypeRepository.existsByCodeIgnoreCaseAndTenantId("BOX", TENANT_ID))
                .thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> productTypeService.createProductType(request, TENANT_ID)
        );

        assertEquals(ErrorCode.PRODUCT_TYPE_CODE_EXISTED, exception.getErrorCode());
        verify(productTypeRepository, never()).save(any(ProductType.class));
    }
}
