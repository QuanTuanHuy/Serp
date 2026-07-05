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
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.usecase.AuthUseCase;
import serp.project.account.core.usecase.RoleUseCase;
import serp.project.account.infrastructure.store.mapper.RoleMapper;
import serp.project.account.kernel.property.AdminProperties;
import serp.project.account.kernel.utils.RoleEnumUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String MODULE_CODE_PTM = "PTM";
    private static final String MODULE_CODE_CRM = "CRM";
    private static final String MODULE_CODE_TMS = "TMS";

    private static final String CLIENT_ID_PTM = "serp-ptm";
    private static final String CLIENT_ID_CRM = "serp-crm";
    private static final String CLIENT_ID_TMS = "serp-first-mile";

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
        createRoleBatch(RoleEnumUtils.getSystemRoles(), null, null, "system roles");
        createRoleBatch(RoleEnumUtils.getOrganizationRoles(), null, null, "organization roles");
        createRoleBatch(RoleEnumUtils.getPtmRoles(), MODULE_CODE_PTM, CLIENT_ID_PTM, "PTM module roles");
        createRoleBatch(RoleEnumUtils.getCrmRoles(), MODULE_CODE_CRM, CLIENT_ID_CRM, "CRM module roles");
        createRoleBatch(RoleEnumUtils.getTmsRoles(), MODULE_CODE_TMS, CLIENT_ID_TMS, "TMS module roles");
    }

    private void createRoleBatch(List<RoleEnum> roleEnums, String moduleCode,
            String keycloakClientId, String batchName) {
        try {
            List<CreateRoleDto> roleDtos = roleMapper.fromRoleEnumListToCreateDto(roleEnums, keycloakClientId);
            if (moduleCode != null && !moduleCode.isBlank()) {
                enrichModuleContext(roleDtos, moduleCode);
            }

            for (var roleDto : roleDtos) {
                try {
                    roleUseCase.createRole(roleDto);
                } catch (Exception e) {
                    log.error("Failed to create role {}: {}", roleDto.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to create {}: {}", batchName, e.getMessage());
        }
    }

    private void enrichModuleContext(List<CreateRoleDto> roleDtos, String moduleCode) {
        var module = moduleService.getModuleByCode(moduleCode);
        if (module == null) {
            log.error("Cannot seed module roles because module code {} is not found", moduleCode);
            return;
        }

        for (CreateRoleDto roleDto : roleDtos) {
            roleDto.setModuleId(module.getId());
            roleDto.setScopeId(module.getId());
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
