package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_bus_app_config")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SchoolBusAppConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_code", nullable = false, length = 100)
    private String configCode;

    @Column(name = "config_name", nullable = false, length = 255)
    private String configName;

    @Column(name = "config_description", columnDefinition = "TEXT")
    private String configDescription;

    @Column(name = "config_type", nullable = false, length = 50)
    private String configType;

    @Column(name = "config_value", nullable = false, length = 1000)
    private String configValue;

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

    public void markCreated(String actor) {
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
}
