/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "menu_displays")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MenuDisplayModel extends BaseModel {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "order_index")
    private Integer order;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;
}
