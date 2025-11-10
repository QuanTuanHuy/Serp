/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.account.core.domain.entity.MenuDisplayEntity;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.enums.MenuType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuDisplayResponse {
    private Long id;
    
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

    private String moduleName;
    private String moduleCode;

    private List<RoleInfo> assignedRoles;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class RoleInfo {
        private Long roleId;
        private String roleName;
    }

    public static MenuDisplayResponse fromEnity(MenuDisplayEntity menuDisplay, ModuleEntity module,
            List<RoleEntity> roles) {
        return MenuDisplayResponse.builder()
                .id(menuDisplay.getId())
                .name(menuDisplay.getName())
                .path(menuDisplay.getPath())
                .icon(menuDisplay.getIcon())
                .order(menuDisplay.getOrder())
                .parentId(menuDisplay.getParentId())
                .moduleId(menuDisplay.getModuleId())
                .menuType(menuDisplay.getMenuType())
                .isVisible(menuDisplay.getIsVisible())
                .description(menuDisplay.getDescription())
                .moduleName(module != null ? module.getModuleName() : null)
                .moduleCode(module != null ? module.getCode() : null)
                .assignedRoles(roles.stream()
                        .map(role -> RoleInfo.builder()
                                .roleId(role.getId())
                                .roleName(role.getName())
                                .build())
                        .toList())
                .build();
    }
}
