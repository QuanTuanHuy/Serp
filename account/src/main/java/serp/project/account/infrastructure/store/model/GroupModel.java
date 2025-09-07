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
@Table(name = "groups")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class GroupModel extends BaseModel {
    @Column(name = "group_name", nullable = false, unique = true, length = 100)
    private String groupName;

    @Column(name = "description")
    private String description;

    @Column(name = "keycloak_group_id", nullable = false, unique = true, length = 100)
    private String keycloakGroupId;
}
