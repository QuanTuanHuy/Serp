/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.domain.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class RoleEntity extends BaseEntity {
    private String name;
    private String description;
    private Boolean isRealmRole;
    private String keycloakClientId;
    private Integer priority;

    private List<PermissionEntity> permissions;
    private List<MenuDisplayEntity> menuDisplays;
}
