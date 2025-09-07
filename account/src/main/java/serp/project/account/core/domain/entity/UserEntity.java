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
public class UserEntity extends BaseEntity {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    private String keycloakId;

    private List<RoleEntity> roles;
    private List<OrganizationEntity> organizations;
}
