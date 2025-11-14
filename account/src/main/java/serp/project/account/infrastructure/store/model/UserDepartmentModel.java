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

@Entity
@Table(name = "user_departments", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id",
        "department_id" }), indexes = {
                @Index(name = "idx_user_departments_user_id", columnList = "user_id"),
                @Index(name = "idx_user_departments_dept_id", columnList = "department_id")
        })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserDepartmentModel extends BaseModel {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
