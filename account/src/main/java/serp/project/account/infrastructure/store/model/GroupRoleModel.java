/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "group_roles", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "role_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class GroupRoleModel extends BaseModel {
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;
}
