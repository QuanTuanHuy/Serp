/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {
    private Long id;
    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;

    public void applyCreate(Long createdBy, Long createdAt) {
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.updatedBy = createdBy;
    }

    public void applyUpdate(Long updatedBy, Long updatedAt) {
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
