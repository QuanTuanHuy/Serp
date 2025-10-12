/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import org.junit.jupiter.api.Test;
import serp.project.account.core.domain.enums.RoleEnum;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoleEnumUtils
 */
class RoleEnumUtilsTest {

    @Test
    void testGetSystemRoles() {
        List<RoleEnum> systemRoles = RoleEnumUtils.getSystemRoles();

        assertTrue(systemRoles.contains(RoleEnum.SUPER_ADMIN));
        assertTrue(systemRoles.stream().allMatch(RoleEnum::isSystemRole));
    }

    @Test
    void testGetOrganizationRoles() {
        List<RoleEnum> orgRoles = RoleEnumUtils.getOrganizationRoles();

        assertTrue(orgRoles.contains(RoleEnum.ORG_OWNER));
        assertTrue(orgRoles.contains(RoleEnum.ORG_ADMIN));
        assertTrue(orgRoles.contains(RoleEnum.ORG_USER));
        assertTrue(orgRoles.stream().allMatch(RoleEnum::isOrganizationRole));
    }

    @Test
    void testGetModuleRoles() {
        List<RoleEnum> moduleRoles = RoleEnumUtils.getModuleRoles();

        assertTrue(moduleRoles.contains(RoleEnum.CRM_ADMIN));
        assertTrue(moduleRoles.contains(RoleEnum.PTM_ADMIN));
        assertTrue(moduleRoles.stream().allMatch(RoleEnum::isModuleRole));
    }

    @Test
    void testGetCrmRoles() {
        List<RoleEnum> crmRoles = RoleEnumUtils.getCrmRoles();

        assertTrue(crmRoles.contains(RoleEnum.CRM_ADMIN));
        assertTrue(crmRoles.contains(RoleEnum.CRM_SALES_MANAGER));
        assertTrue(crmRoles.contains(RoleEnum.CRM_SALES_PERSON));
        assertTrue(crmRoles.stream().allMatch(role -> role.getRoleName().startsWith("CRM_")));
    }

    @Test
    void testGetHighestPriorityRole() {
        List<RoleEnum> roles = Arrays.asList(
                RoleEnum.ORG_USER, // priority 7
                RoleEnum.CRM_SALES_MANAGER, // priority 6
                RoleEnum.ORG_ADMIN // priority 3
        );

        Optional<RoleEnum> highest = RoleEnumUtils.getHighestPriorityRole(roles);

        assertTrue(highest.isPresent());
        assertEquals(RoleEnum.ORG_ADMIN, highest.get());
        assertEquals(3, highest.get().getPriority());
    }

    @Test
    void testSortByPriority() {
        List<RoleEnum> roles = Arrays.asList(
                RoleEnum.ORG_USER,
                RoleEnum.SUPER_ADMIN,
                RoleEnum.ORG_ADMIN);

        List<RoleEnum> sorted = RoleEnumUtils.sortByPriority(roles);

        assertEquals(RoleEnum.SUPER_ADMIN, sorted.get(0)); // priority 1
        assertEquals(RoleEnum.ORG_ADMIN, sorted.get(1)); // priority 3
        assertEquals(RoleEnum.ORG_USER, sorted.get(2)); // priority 7
    }

    @Test
    void testHasAdminRole() {
        List<RoleEnum> adminRoles = Arrays.asList(RoleEnum.ORG_ADMIN, RoleEnum.ORG_USER);
        List<RoleEnum> nonAdminRoles = Arrays.asList(RoleEnum.ORG_USER, RoleEnum.ORG_MODERATOR);

        assertTrue(RoleEnumUtils.hasAdminRole(adminRoles));
        assertFalse(RoleEnumUtils.hasAdminRole(nonAdminRoles));
    }

    @Test
    void testHasManagerRole() {
        List<RoleEnum> managerRoles = Arrays.asList(RoleEnum.CRM_SALES_MANAGER, RoleEnum.ORG_USER);
        List<RoleEnum> nonManagerRoles = Arrays.asList(RoleEnum.ORG_USER, RoleEnum.CRM_SALES_PERSON);

        assertTrue(RoleEnumUtils.hasManagerRole(managerRoles));
        assertFalse(RoleEnumUtils.hasManagerRole(nonManagerRoles));
    }

    @Test
    void testCanManageUsers() {
        assertTrue(RoleEnumUtils.canManageUsers(RoleEnum.SUPER_ADMIN));
        assertTrue(RoleEnumUtils.canManageUsers(RoleEnum.ORG_OWNER));
        assertTrue(RoleEnumUtils.canManageUsers(RoleEnum.ORG_ADMIN));
        assertTrue(RoleEnumUtils.canManageUsers(RoleEnum.CRM_SALES_MANAGER));
        assertFalse(RoleEnumUtils.canManageUsers(RoleEnum.ORG_USER));
        assertFalse(RoleEnumUtils.canManageUsers(RoleEnum.CRM_SALES_PERSON));
    }

    @Test
    void testCanManageModules() {
        assertTrue(RoleEnumUtils.canManageModules(RoleEnum.SUPER_ADMIN));
        assertTrue(RoleEnumUtils.canManageModules(RoleEnum.ORG_OWNER));
        assertTrue(RoleEnumUtils.canManageModules(RoleEnum.ORG_ADMIN));
        assertFalse(RoleEnumUtils.canManageModules(RoleEnum.CRM_ADMIN));
        assertFalse(RoleEnumUtils.canManageModules(RoleEnum.ORG_USER));
    }

    @Test
    void testFromRoleNames() {
        List<String> roleNames = Arrays.asList("SUPER_ADMIN", "ORG_ADMIN", "CRM_ADMIN");
        List<RoleEnum> roles = RoleEnumUtils.fromRoleNames(roleNames);

        assertEquals(3, roles.size());
        assertTrue(roles.contains(RoleEnum.SUPER_ADMIN));
        assertTrue(roles.contains(RoleEnum.ORG_ADMIN));
        assertTrue(roles.contains(RoleEnum.CRM_ADMIN));
    }

    @Test
    void testToRoleNames() {
        List<RoleEnum> roles = Arrays.asList(
                RoleEnum.SUPER_ADMIN,
                RoleEnum.ORG_ADMIN,
                RoleEnum.CRM_ADMIN);
        List<String> roleNames = RoleEnumUtils.toRoleNames(roles);

        assertEquals(3, roleNames.size());
        assertTrue(roleNames.contains("SUPER_ADMIN"));
        assertTrue(roleNames.contains("ORG_ADMIN"));
        assertTrue(roleNames.contains("CRM_ADMIN"));
    }

    @Test
    void testFromRoleNameSafe() {
        Optional<RoleEnum> validRole = RoleEnumUtils.fromRoleNameSafe("SUPER_ADMIN");
        Optional<RoleEnum> invalidRole = RoleEnumUtils.fromRoleNameSafe("INVALID_ROLE");

        assertTrue(validRole.isPresent());
        assertEquals(RoleEnum.SUPER_ADMIN, validRole.get());
        assertFalse(invalidRole.isPresent());
    }

    @Test
    void testGetDefaultOrganizationRole() {
        RoleEnum defaultRole = RoleEnumUtils.getDefaultOrganizationRole();
        assertEquals(RoleEnum.ORG_USER, defaultRole);
    }

    @Test
    void testGetDefaultModuleRole() {
        assertEquals(RoleEnum.CRM_SALES_PERSON, RoleEnumUtils.getDefaultModuleRole("CRM"));
        assertEquals(RoleEnum.PTM_USER, RoleEnumUtils.getDefaultModuleRole("PTM"));
        assertEquals(RoleEnum.ACCOUNTANT, RoleEnumUtils.getDefaultModuleRole("ACCOUNTING"));
        assertEquals(RoleEnum.MODULE_USER, RoleEnumUtils.getDefaultModuleRole("UNKNOWN"));
    }

    @Test
    void testGetPromotedRole() {
        // CRM promotions
        assertEquals(
                Optional.of(RoleEnum.CRM_SALES_MANAGER),
                RoleEnumUtils.getPromotedRole(RoleEnum.CRM_SALES_PERSON));
        assertEquals(
                Optional.of(RoleEnum.CRM_ADMIN),
                RoleEnumUtils.getPromotedRole(RoleEnum.CRM_SALES_MANAGER));

        // Organization promotions
        assertEquals(
                Optional.of(RoleEnum.ORG_MODERATOR),
                RoleEnumUtils.getPromotedRole(RoleEnum.ORG_USER));
        assertEquals(
                Optional.of(RoleEnum.ORG_ADMIN),
                RoleEnumUtils.getPromotedRole(RoleEnum.ORG_MODERATOR));
    }

    @Test
    void testGetDemotedRole() {
        // CRM demotions
        assertEquals(
                Optional.of(RoleEnum.CRM_SALES_MANAGER),
                RoleEnumUtils.getDemotedRole(RoleEnum.CRM_ADMIN));
        assertEquals(
                Optional.of(RoleEnum.CRM_SALES_PERSON),
                RoleEnumUtils.getDemotedRole(RoleEnum.CRM_SALES_MANAGER));

        // Organization demotions
        assertEquals(
                Optional.of(RoleEnum.ORG_MODERATOR),
                RoleEnumUtils.getDemotedRole(RoleEnum.ORG_ADMIN));
        assertEquals(
                Optional.of(RoleEnum.ORG_USER),
                RoleEnumUtils.getDemotedRole(RoleEnum.ORG_MODERATOR));
    }

    @Test
    void testGetDisplayName() {
        assertEquals("Super admin", RoleEnumUtils.getDisplayName(RoleEnum.SUPER_ADMIN));
        assertEquals("Org owner", RoleEnumUtils.getDisplayName(RoleEnum.ORG_OWNER));
        assertEquals("Crm sales manager", RoleEnumUtils.getDisplayName(RoleEnum.CRM_SALES_MANAGER));
    }

    @Test
    void testGetBadgeColor() {
        assertEquals("purple", RoleEnumUtils.getBadgeColor(RoleEnum.ORG_OWNER));
        assertEquals("red", RoleEnumUtils.getBadgeColor(RoleEnum.SUPER_ADMIN));
        assertEquals("blue", RoleEnumUtils.getBadgeColor(RoleEnum.CRM_SALES_MANAGER));
        assertEquals("green", RoleEnumUtils.getBadgeColor(RoleEnum.ORG_USER));
        assertEquals("gray", RoleEnumUtils.getBadgeColor(RoleEnum.CRM_VIEWER));
    }

    @Test
    void testGetIcon() {
        assertEquals("crown", RoleEnumUtils.getIcon(RoleEnum.ORG_OWNER));
        assertEquals("shield", RoleEnumUtils.getIcon(RoleEnum.SUPER_ADMIN));
        assertEquals("star", RoleEnumUtils.getIcon(RoleEnum.CRM_SALES_MANAGER));
        assertEquals("user", RoleEnumUtils.getIcon(RoleEnum.ORG_USER));
        assertEquals("eye", RoleEnumUtils.getIcon(RoleEnum.CRM_VIEWER));
    }

    @Test
    void testGetRolesByScope() {
        List<RoleEnum> orgRoles = RoleEnumUtils.getRolesByScope(RoleScope.ORGANIZATION);
        assertTrue(orgRoles.stream().allMatch(role -> role.getScope() == RoleScope.ORGANIZATION));

        List<RoleEnum> moduleRoles = RoleEnumUtils.getRolesByScope(RoleScope.MODULE);
        assertTrue(moduleRoles.stream().allMatch(role -> role.getScope() == RoleScope.MODULE));
    }

    @Test
    void testGetRolesByType() {
        List<RoleEnum> adminRoles = RoleEnumUtils.getRolesByType(RoleType.ADMIN);
        assertTrue(adminRoles.stream().allMatch(role -> role.getType() == RoleType.ADMIN));

        List<RoleEnum> managerRoles = RoleEnumUtils.getRolesByType(RoleType.MANAGER);
        assertTrue(managerRoles.stream().allMatch(role -> role.getType() == RoleType.MANAGER));
    }

    @Test
    void testGetAdminRoles() {
        List<RoleEnum> adminRoles = RoleEnumUtils.getAdminRoles();

        assertTrue(adminRoles.stream().allMatch(
                role -> role.getType() == RoleType.ADMIN || role.getType() == RoleType.OWNER));
        assertTrue(adminRoles.contains(RoleEnum.SUPER_ADMIN));
        assertTrue(adminRoles.contains(RoleEnum.ORG_OWNER));
        assertTrue(adminRoles.contains(RoleEnum.ORG_ADMIN));
    }

    @Test
    void testGetRealmRoles() {
        List<RoleEnum> realmRoles = RoleEnumUtils.getRealmRoles();
        assertTrue(realmRoles.stream().allMatch(RoleEnum::getIsRealmRole));
        assertTrue(realmRoles.contains(RoleEnum.SUPER_ADMIN));
    }

    @Test
    void testGetClientRoles() {
        List<RoleEnum> clientRoles = RoleEnumUtils.getClientRoles();
        assertTrue(clientRoles.stream().noneMatch(RoleEnum::getIsRealmRole));
        assertTrue(clientRoles.contains(RoleEnum.CRM_ADMIN));
        assertTrue(clientRoles.contains(RoleEnum.PTM_ADMIN));
    }
}
