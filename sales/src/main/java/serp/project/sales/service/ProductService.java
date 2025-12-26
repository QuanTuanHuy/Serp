package serp.project.sales.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.sales.dto.request.ProductCreationForm;
import serp.project.sales.dto.request.ProductUpdateForm;
import serp.project.sales.entity.ProductEntity;
import serp.project.sales.repository.ProductRepository;
import serp.project.sales.repository.specification.ProductSpecification;
import serp.project.sales.util.PaginationUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductCreationForm form, Long tenantId) {
        ProductEntity product = new ProductEntity(form, tenantId);
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
        product.update(form);
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

    public List<ProductEntity> getProducts(List<String> productIds, Long tenantId) {
        List<ProductEntity> products = productRepository.findAllById(productIds);
        products.removeIf(product -> !product.getTenantId().equals(tenantId));
        return products;
    }

}
