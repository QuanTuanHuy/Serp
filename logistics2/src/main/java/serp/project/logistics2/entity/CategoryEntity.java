package serp.project.logistics2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.dto.request.CategoryForm;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_product_category")
public class CategoryEntity {

    @Id
    private String id;

    private String name;

    @Column(name = "tenant_id")
    private Long tenantId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    public CategoryEntity(CategoryForm form, Long tenantId) {
        String categoryId = IdUtils.generateCategoryId();
        this.id = categoryId;
        this.name = form.getName();
        this.tenantId = tenantId;
    }

    public void update(CategoryForm form) {
        if (StringUtils.hasText(form.getName()))
            this.name = form.getName();
    }
}
