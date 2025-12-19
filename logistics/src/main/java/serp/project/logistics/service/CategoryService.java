package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.logistics.dto.request.CategoryForm;
import serp.project.logistics.entity.CategoryEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.repository.CategoryRepository;
import serp.project.logistics.repository.specification.CategorySpecification;
import serp.project.logistics.util.IdUtils;
import serp.project.logistics.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CategoryForm form, Long tenantId) {
        String categoryId = IdUtils.generateCategoryId();
        var category = new CategoryEntity(form, tenantId);
        categoryRepository.save(category);
        log.info("[CategoryService] Created category {} with ID {} for tenantId: {}", category.getName(), categoryId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(String categoryId, CategoryForm form, Long tenantId) {
        var category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null || !category.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        category.update(form);
        categoryRepository.save(category);
        log.info("[CategoryService] Updated category {} with ID {} for tenantId: {}", category.getName(), categoryId,
                tenantId);
    }

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
