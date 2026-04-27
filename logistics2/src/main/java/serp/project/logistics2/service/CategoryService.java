package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.logistics2.entity.CategoryEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.CategoryRepository;
import serp.project.logistics2.repository.specification.CategorySpecification;
import serp.project.logistics2.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryEntity getCategory(String categoryId, Long tenantId) {
        var category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null || !category.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        log.info("[CategoryService] Retrieved category {} with ID {} for tenantId: {}", category.getName(), categoryId,
                tenantId);
        return category;
    }

    public Page<CategoryEntity> getCategories(
            String query,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return categoryRepository.findAll(
                CategorySpecification.satisfy(query, tenantId),
                pageable);
    }

}
