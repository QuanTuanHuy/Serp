package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.dto.request.ProductCreationForm;
import serp.project.purchase_service.dto.request.ProductUpdateForm;
import serp.project.purchase_service.entity.ProductEntity;
import serp.project.purchase_service.repository.ProductRepository;
import serp.project.purchase_service.repository.specification.ProductSpecification;
import serp.project.purchase_service.util.IdUtils;
import serp.project.purchase_service.util.PaginationUtils;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductCreationForm form, Long tenantId) {
        String productId = IdUtils.generateProductId();
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name(form.getName())
                .weight(form.getWeight())
                .height(form.getHeight())
                .unit(form.getUnit())
                .costPrice(form.getCostPrice())
                .wholeSalePrice(form.getWholeSalePrice())
                .retailPrice(form.getRetailPrice())
                .categoryId(form.getCategoryId())
                .statusId(form.getStatusId())
                .imageId(form.getImageId())
                .extraProps(form.getExtraProps())
                .vatRate(form.getVatRate())
                .skuCode(form.getSkuCode())
                .tenantId(tenantId)
                .build();
        productRepository.save(product);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(String productId, ProductUpdateForm form, Long tenantId) {
        ProductEntity product = productRepository.findById(productId).orElse(null);
        if (product == null || !product.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Product not found or access denied");
        }
        product.setName(form.getName());
        product.setWeight(form.getWeight());
        product.setHeight(form.getHeight());
        product.setUnit(form.getUnit());
        product.setCostPrice(form.getCostPrice());
        product.setWholeSalePrice(form.getWholeSalePrice());
        product.setRetailPrice(form.getRetailPrice());
        product.setStatusId(form.getStatusId());
        product.setImageId(form.getImageId());
        product.setExtraProps(form.getExtraProps());
        product.setVatRate(form.getVatRate());
        product.setSkuCode(form.getSkuCode());

        productRepository.save(product);
    }

    public Page<ProductEntity> findProducts(
            String query,
            String categoryId,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return productRepository.findAll(
                ProductSpecification.satisfy(query, categoryId, statusId, tenantId),
                pageable);
    }

    public ProductEntity getProduct(String productId, Long tenantId) {
        ProductEntity product = productRepository.findById(productId).orElse(null);
        if (product != null && !product.getTenantId().equals(tenantId)) {
            return null;
        }
        return product;
    }

}
