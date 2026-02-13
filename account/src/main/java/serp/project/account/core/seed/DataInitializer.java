/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.seed;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.enums.RoleEnum;
import serp.project.account.core.usecase.AuthUseCase;
import serp.project.account.core.usecase.RoleUseCase;
import serp.project.account.core.service.IModuleService;
import serp.project.account.infrastructure.store.mapper.RoleMapper;
import serp.project.account.kernel.property.AdminProperties;
import serp.project.account.kernel.utils.RoleEnumUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleUseCase roleUseCase;
    private final IModuleService moduleService;

    private final RoleMapper roleMapper;

    private final AuthUseCase authUseCase;

    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        log.info("Initializing data...");

        try {
            seedPredefinedModules();
        } catch (Exception e) {
            log.error("Module seeding failed: {}", e.getMessage());
        }

        try {
            createRoles();
        } catch (Exception e) {
            log.error("Data initialization failed: {}", e.getMessage());
        }

        try {
            createSuperAdminUser();
        } catch (Exception e) {
            log.error("Data initialization failed: {}", e.getMessage());
        }
    }

    private void seedPredefinedModules() {
        try {
            log.info("Starting to seed predefined modules...");
            moduleService.seedPredefinedModules();
            log.info("Successfully seeded predefined modules");
        } catch (Exception e) {
            log.error("Failed to seed predefined modules: {}", e.getMessage(), e);
        }
    }

    private void createRoles() {
        // create realm roles
        try {
            List<RoleEnum> systemRoles = RoleEnumUtils.getSystemRoles();
            List<CreateRoleDto> roleDtos = roleMapper.fromRoleEnumListToCreateDto(systemRoles);
            for (var roleDto : roleDtos) {
                try {
                    roleUseCase.createRole(roleDto);
                } catch (Exception e) {
                    log.error("Failed to create role {}: {}", roleDto.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create system roles: {}", e.getMessage());
        }
        try {
            List<RoleEnum> orgRoles = RoleEnumUtils.getOrganizationRoles();
            List<CreateRoleDto> roleDtos = roleMapper.fromRoleEnumListToCreateDto(orgRoles);
            for (var roleDto : roleDtos) {
                try {
                    roleUseCase.createRole(roleDto);
                } catch (Exception e) {
                    log.error("Failed to create role {}: {}", roleDto.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create organization roles: {}", e.getMessage());
        }

        // create client roles for each module
        // PTM
        try {
            List<RoleEnum> ptmRoles = RoleEnumUtils.getPtmRoles();
            List<CreateRoleDto> roleDtos = roleMapper.fromRoleEnumListToCreateDto(ptmRoles, "serp-ptm");
            for (var roleDto : roleDtos) {
                try {
                    roleUseCase.createRole(roleDto);
                } catch (Exception e) {
                    log.error("Failed to create role {}: {}", roleDto.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create PTM module roles: {}", e.getMessage());
        }
        // CRM
        try {
            List<RoleEnum> crmRoles = RoleEnumUtils.getCrmRoles();
            List<CreateRoleDto> roleDtos = roleMapper.fromRoleEnumListToCreateDto(crmRoles, "serp-crm");
            for (var roleDto : roleDtos) {
                try {
                    roleUseCase.createRole(roleDto);
                } catch (Exception e) {
                    log.error("Failed to create role {}: {}", roleDto.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create CRM module roles: {}", e.getMessage());
        }
    }

    private void createSuperAdminUser() {
        var response = authUseCase.createSuperAdmin(adminProperties.getEmail(), adminProperties.getPassword());
        if (!response.isSuccess()) {
            log.error("Super Admin creation failed: {}", response.getMessage());
            return;
        }
        log.info("Super Admin create successfully");
    }

}
