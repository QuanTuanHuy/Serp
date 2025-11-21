package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.dto.request.CategoryForm;
import serp.project.purchase_service.entity.CategoryEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.CategoryRepository;
import serp.project.purchase_service.repository.specification.CategorySpecification;
import serp.project.purchase_service.util.IdUtils;
import serp.project.purchase_service.util.PaginationUtils;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CategoryForm form, Long tenantId) {
        String categoryId = IdUtils.generateCategoryId();
        var category = CategoryEntity.builder()
                .id(categoryId)
                .name(form.getName())
                .tenantId(tenantId)
                .build();
        categoryRepository.save(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(String categoryId, CategoryForm form, Long tenantId) {
        var category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null || !category.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        category.setName(form.getName());
        categoryRepository.save(category);
    }

    public CategoryEntity getCategory(String categoryId, Long tenantId) {
        var category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null || !category.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        return category;
    }

    public Page<CategoryEntity> getCategories(
            String query,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return categoryRepository.findAll(
                CategorySpecification.satisfy(query, tenantId),
                pageable
        );
    }

}
