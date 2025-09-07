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
@Table(name = "menu_display_roles", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "menu_display_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MenuDisplayRoleModel extends BaseModel {
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "menu_display_id", nullable = false)
    private Long menuDisplayId;
}
