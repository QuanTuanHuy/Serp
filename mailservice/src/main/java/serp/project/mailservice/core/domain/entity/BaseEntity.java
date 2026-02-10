/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.ActiveStatus;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ActiveStatus activeStatus;

    public boolean isDeleted() {
        return ActiveStatus.DELETED.equals(activeStatus);
    }

    public void markAsDeleted() {
        this.activeStatus = ActiveStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    protected void initializeDefaults() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.activeStatus == null) {
            this.activeStatus = ActiveStatus.ACTIVE;
        }
    }
}
