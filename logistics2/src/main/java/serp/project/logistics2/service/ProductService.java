package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.logistics2.entity.ProductEntity;
import serp.project.logistics2.repository.ProductRepository;
import serp.project.logistics2.repository.specification.ProductSpecification;
import serp.project.logistics2.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

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
