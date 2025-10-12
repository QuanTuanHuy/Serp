/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_module_access", indexes = {
        @Index(name = "idx_user_module_org", columnList = "user_id,module_id,organization_id", unique = true),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_module_id", columnList = "module_id"),
        @Index(name = "idx_organization_id", columnList = "organization_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserModuleAccessModel extends BaseModel {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "description", length = 500)
    private String description;
}
