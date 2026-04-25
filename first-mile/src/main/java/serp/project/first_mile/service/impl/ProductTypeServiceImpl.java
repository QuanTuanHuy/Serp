/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.ProductType;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreateProductTypeRequest;
import serp.project.first_mile.dto.request.UpdateProductTypeRequest;
import serp.project.first_mile.dto.response.ProductTypeResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.mapper.ProductTypeMapper;
import serp.project.first_mile.repository.ProductTypeRepository;
import serp.project.first_mile.service.ProductTypeService;

@Service
@RequiredArgsConstructor
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;
    private final FirstMileAccessUtils firstMileAccessUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductTypeResponse> getProductTypes(int page, int size, String keyword) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String normalizedKeyword = normalizeKeyword(keyword);

        Page<ProductTypeResponse> mappedPage = productTypeRepository.searchByTenantId(tenantId, normalizedKeyword, pageable)
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
    @Transactional(readOnly = true)
    public ProductTypeResponse getProductTypeById(Long id) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        ProductType productType = getProductTypeByIdAndTenantOrThrow(id, tenantId);
        return ProductTypeMapper.toResponse(productType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductTypeResponse createProductType(CreateProductTypeRequest request) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
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
    public ProductTypeResponse updateProductType(Long id, UpdateProductTypeRequest request) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
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
    public void deleteProductType(Long id) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
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