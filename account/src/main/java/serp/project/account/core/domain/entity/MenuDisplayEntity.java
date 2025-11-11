/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.MenuType;

/**
 * Menu Display entity - định nghĩa UI menus trong modules
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MenuDisplayEntity extends BaseEntity {

    private String name;

    private String path;

    private String icon;

    private Integer order;

    private Long parentId;

    private Long moduleId;

    private MenuType menuType;

    @Builder.Default
    private Boolean isVisible = true;

    private String description;

    private List<MenuDisplayRoleEntity> assignedRoles;
}
