/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CreateProductTypeRequest;
import serp.project.tms_order.dto.request.UpdateProductTypeRequest;
import serp.project.tms_order.dto.response.ProductTypeResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.mapper.ProductTypeMapper;
import serp.project.tms_order.repository.ProductTypeRepository;
import serp.project.tms_order.service.ProductTypeService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    @Override
    public PageResponse<ProductTypeResponse> getProductTypes(int page, int size, String keyword, Long tenantId) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String normalizedKeyword = normalizeKeyword(keyword);

        Page<ProductTypeResponse> mappedPage = productTypeRepository
                .searchByTenantId(tenantId, normalizedKeyword, pageable)
                .map(ProductTypeMapper::toResponse);

        return PageResponse.<ProductTypeResponse>builder()
                .items(mappedPage.getContent())
                .page(mappedPage.getNumber())
                .size(mappedPage.getSize())
                .totalElements(mappedPage.getTotalElements())
                .totalPages(mappedPage.getTotalPages())
                .hasNext(mappedPage.hasNext())
                .hasPrevious(mappedPage.hasPrevious())
                .build();
    }

    @Override
    public ProductTypeResponse getProductTypeById(Long id, Long tenantId) {
        ProductType productType = getProductTypeByIdAndTenantOrThrow(id, tenantId);
        return ProductTypeMapper.toResponse(productType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductTypeResponse createProductType(CreateProductTypeRequest request, Long tenantId) {
        String normalizedCode = normalizeCode(request.getCode());

        if (productTypeRepository.existsByCodeIgnoreCaseAndTenantId(normalizedCode, tenantId)) {
            throw new AppException(ErrorCode.PRODUCT_TYPE_CODE_EXISTED);
        }

        ProductType productType = ProductTypeMapper.toEntity(request);
        productType.setCode(normalizedCode);
        productType.setTenantId(tenantId);

        ProductType savedProductType = productTypeRepository.save(productType);
        return ProductTypeMapper.toResponse(savedProductType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductTypeResponse updateProductType(Long id, UpdateProductTypeRequest request, Long tenantId) {
        ProductType productType = getProductTypeByIdAndTenantOrThrow(id, tenantId);
        String normalizedCode = normalizeCode(request.getCode());

        if (productTypeRepository.existsByCodeIgnoreCaseAndTenantIdAndIdNot(normalizedCode, tenantId, id)) {
            throw new AppException(ErrorCode.PRODUCT_TYPE_CODE_EXISTED);
        }

        ProductTypeMapper.mapForUpdate(request, productType);
        productType.setCode(normalizedCode);
        productType.setTenantId(tenantId);

        ProductType updatedProductType = productTypeRepository.save(productType);
        return ProductTypeMapper.toResponse(updatedProductType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductType(Long id, Long tenantId) {
        ProductType productType = getProductTypeByIdAndTenantOrThrow(id, tenantId);
        productTypeRepository.delete(productType);
    }

    private ProductType getProductTypeByIdAndTenantOrThrow(Long id, Long tenantId) {
        return productTypeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_TYPE_NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
