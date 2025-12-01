package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.logistics.dto.request.ProductCreationForm;
import serp.project.logistics.dto.request.ProductUpdateForm;
import serp.project.logistics.entity.ProductEntity;
import serp.project.logistics.repository.ProductRepository;
import serp.project.logistics.repository.specification.ProductSpecification;
import serp.project.logistics.util.IdUtils;
import serp.project.logistics.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("[ProductService] Created product {} with ID {} for tenantId {}", product.getId(), product.getId(),
                tenantId);
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
        log.info("[ProductService] Updated product {} with ID {} for tenantId {}", product.getName(), product.getId(),
                tenantId);
    }

    public Page<ProductEntity> findProducts(
            String query,
            String categoryId,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return productRepository.findAll(
                ProductSpecification.satisfy(query, categoryId, statusId, tenantId),
                pageable);
    }

    public ProductEntity getProduct(String productId, Long tenantId) {
        ProductEntity product = productRepository.findById(productId).orElse(null);
        if (product != null && !product.getTenantId().equals(tenantId)) {
            log.info("[ProductService] Product with ID {} does not exist or access denied for tenantId {}", productId,
                    tenantId);
            return null;
        }
        return product;
    }

}
