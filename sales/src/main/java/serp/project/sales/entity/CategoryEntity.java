package serp.project.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;
import serp.project.sales.dto.request.CategoryForm;
import serp.project.sales.util.IdUtils;

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
        this.id = IdUtils.generateCategoryId();
        this.name = form.getName();
        this.tenantId = tenantId;
    }

    public void update(CategoryForm form) {
        if (StringUtils.hasText(form.getName()))
            this.name = form.getName();
    }
}
