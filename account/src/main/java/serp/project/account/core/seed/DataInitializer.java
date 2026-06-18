/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.seed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.request.CreateMenuDisplayDto;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.MenuDisplayEntity;
import serp.project.account.core.domain.enums.RoleEnum;
import serp.project.account.core.usecase.AuthUseCase;
import serp.project.account.core.usecase.RoleUseCase;
import serp.project.account.core.service.IMenuDisplayService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IRoleService;
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
    private static final String MODULE_CODE_SCHOOL_BUS = "SCHOOLBUS";

    private static final String CLIENT_ID_PTM = "serp-ptm";
    private static final String CLIENT_ID_CRM = "serp-crm";
    private static final String CLIENT_ID_TMS = "serp-first-mile";
    private static final String CLIENT_ID_SCHOOL_BUS = "serp-school-bus";

    private static final String ROLE_TMS_ADMIN = "TMS_ADMIN";

    private final RoleUseCase roleUseCase;
    private final IModuleService moduleService;
    private final IRoleService roleService;
    private final IMenuDisplayService menuDisplayService;

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
            seedFirstMileMenus();
        } catch (Exception e) {
            log.error("First-mile menu seeding failed: {}", e.getMessage());
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
        createRoleBatch(RoleEnumUtils.getSchoolBusRoles(), MODULE_CODE_SCHOOL_BUS, CLIENT_ID_SCHOOL_BUS, "School Bus module roles");
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

    private void seedFirstMileMenus() {
        var module = moduleService.getModuleByCode(MODULE_CODE_TMS);
        if (module == null) {
            log.warn("Skip first-mile menu seed because module {} is missing", MODULE_CODE_TMS);
            return;
        }

        List<CreateMenuDisplayDto> menuSeeds = List.of(
                CreateMenuDisplayDto.builder()
                        .name("Post Offices")
                        .path("/first-mile/post-offices")
                        .icon("building")
                        .order(1)
                        .moduleId(module.getId())
                        .menuType("SIDEBAR")
                        .isVisible(true)
                        .description("First-mile post offices")
                        .build(),
                CreateMenuDisplayDto.builder()
                        .name("Product Types")
                        .path("/first-mile/product-types")
                        .icon("package")
                        .order(2)
                        .moduleId(module.getId())
                        .menuType("SIDEBAR")
                        .isVisible(true)
                        .description("First-mile product types")
                        .build(),
                CreateMenuDisplayDto.builder()
                        .name("Import History")
                        .path("/first-mile/import-history")
                        .icon("history")
                        .order(3)
                        .moduleId(module.getId())
                        .menuType("SIDEBAR")
                        .isVisible(true)
                        .description("First-mile import history")
                        .build());

        List<Long> menuIds = new ArrayList<>();
        for (CreateMenuDisplayDto seed : menuSeeds) {
            var existing = menuDisplayService.getMenuDisplayByModuleIdAndName(module.getId(), seed.getName());
            if (existing != null) {
                menuIds.add(existing.getId());
                continue;
            }

            var created = menuDisplayService.createMenuDisplay(seed);
            menuIds.add(created.getId());
        }

        assignMenusToRole(ROLE_TMS_ADMIN, menuIds);
    }

    private void assignMenusToRole(String roleName, List<Long> menuIds) {
        var role = roleService.getRoleByName(roleName);
        if (role == null || menuIds.isEmpty()) {
            log.warn("Skip menu assignment for role {} because role or menus are missing", roleName);
            return;
        }

        Map<Long, List<MenuDisplayEntity>> byRole =
                menuDisplayService.getMenuDisplaysByRoleIds(List.of(role.getId()));
        Set<Long> existingMenuIds = byRole.getOrDefault(role.getId(), List.of()).stream()
            .map(MenuDisplayEntity::getId)
                .collect(Collectors.toSet());

        List<Long> menuIdsToAssign = menuIds.stream()
                .filter(id -> !existingMenuIds.contains(id))
                .toList();
        if (menuIdsToAssign.isEmpty()) {
            return;
        }

        menuDisplayService.assignMenuDisplaysToRole(role.getId(), menuIdsToAssign);
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
