package serp.project.school_bus_service.shared.base;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public void markCreated(Long tenantId, String actor) {
        this.tenantId = tenantId;
        this.isDeleted = Boolean.FALSE;
        this.isActive = this.isActive == null ? Boolean.TRUE : this.isActive;
        this.createdBy = actor;
        this.updatedBy = actor;
    }

    public void markUpdated(String actor) {
        this.updatedBy = actor;
    }

    public void markSoftDeleted(String actor) {
        this.isDeleted = Boolean.TRUE;
        this.isActive = Boolean.FALSE;
        this.updatedBy = actor;
    }

    public void restore(String actor) {
        this.isDeleted = Boolean.FALSE;
        this.isActive = Boolean.TRUE;
        this.updatedBy = actor;
    }
}
