/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.service;

import java.util.List;

import serp.project.account.core.domain.dto.request.CreateClientRoleDto;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.RoleEntity;

public interface IRoleService {
    RoleEntity createRole(CreateRoleDto request);
    RoleEntity getRoleByName(String name);
    List<RoleEntity> getAllRoles();
    RoleEntity createRealmRole(CreateRoleDto request);
    RoleEntity createClientRole(CreateClientRoleDto request);
}
