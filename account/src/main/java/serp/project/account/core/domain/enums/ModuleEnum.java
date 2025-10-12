/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Predefined system modules (apps) trong SERP platform
 * Mỗi module đại diện cho một business domain/feature set
 */
@Getter
public enum ModuleEnum {
    // ==================== SALES & MARKETING ====================
    /**
     * CRM - Customer Relationship Management
     * Quản lý khách hàng, leads, opportunities, activities
     */
    CRM(
            "CRM",
            "Customer Relationship Management",
            "serp-crm",
            "Sales & Marketing",
            "users",
            1,
            ModuleType.SYSTEM,
            true,
            true,
            PricingModel.FREE,
            ModuleStatus.ACTIVE,
            "1.0.0",
            List.of()),

    /**
     * SALES - Sales Management
     * Quản lý đơn hàng, quotes, invoices
     */
    SALES(
            "SALES",
            "Sales Management",
            "serp-sales",
            "Sales & Marketing",
            "shopping-cart",
            2,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.9.0",
            List.of("CRM")),

    /**
     * MARKETING - Marketing Automation
     * Email campaigns, social media, analytics
     */
    MARKETING(
            "MARKETING",
            "Marketing Automation",
            "serp-marketing",
            "Sales & Marketing",
            "megaphone",
            3,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.8.0",
            List.of("CRM")),

    // ==================== PRODUCTIVITY ====================
    /**
     * PTM - Personal Task Management
     * Quản lý tasks, schedules, optimization cá nhân
     */
    PTM(
            "PTM",
            "Personal Task Management",
            "serp-ptm",
            "Productivity",
            "check-square",
            4,
            ModuleType.SYSTEM,
            true,
            true,
            PricingModel.FREE,
            ModuleStatus.ACTIVE,
            "1.0.0",
            List.of()),

    /**
     * PROJECT - Project Management
     * Quản lý dự án, sprints, kanban boards
     */
    PROJECT(
            "PROJECT",
            "Project Management",
            "serp-project",
            "Productivity",
            "briefcase",
            5,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.7.0",
            List.of("PTM")),

    // ==================== FINANCE & ACCOUNTING ====================
    /**
     * ACCOUNTING - Accounting & Finance
     * Quản lý tài chính, kế toán, báo cáo
     */
    ACCOUNTING(
            "ACCOUNTING",
            "Accounting & Finance",
            "serp-accounting",
            "Finance",
            "calculator",
            6,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.MAINTENANCE,
            "0.5.0",
            List.of("SALES")),

    /**
     * INVOICING - Invoicing & Billing
     * Quản lý hóa đơn, thanh toán
     */
    INVOICING(
            "INVOICING",
            "Invoicing & Billing",
            "serp-invoicing",
            "Finance",
            "file-text",
            7,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.6.0",
            List.of("SALES", "ACCOUNTING")),

    // ==================== HUMAN RESOURCES ====================
    /**
     * HR - Human Resources
     * Quản lý nhân sự, attendance, payroll
     */
    HR(
            "HR",
            "Human Resources",
            "serp-hr",
            "Human Resources",
            "users-cog",
            8,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.6.0",
            List.of()),

    /**
     * RECRUITMENT - Recruitment Management
     * Quản lý tuyển dụng, job postings, candidates
     */
    RECRUITMENT(
            "RECRUITMENT",
            "Recruitment Management",
            "serp-recruitment",
            "Human Resources",
            "user-plus",
            9,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.5.0",
            List.of("HR")),

    // ==================== OPERATIONS ====================
    /**
     * INVENTORY - Inventory Management
     * Quản lý kho hàng, stock, warehouses
     */
    INVENTORY(
            "INVENTORY",
            "Inventory Management",
            "serp-inventory",
            "Operations",
            "warehouse",
            10,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.5.0",
            List.of("SALES")),

    /**
     * MANUFACTURING - Manufacturing Management
     * Quản lý sản xuất, bill of materials, work orders
     */
    MANUFACTURING(
            "MANUFACTURING",
            "Manufacturing Management",
            "serp-manufacturing",
            "Operations",
            "cogs",
            11,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.4.0",
            List.of("INVENTORY")),

    // ==================== SUPPORT & SERVICE ====================
    /**
     * HELPDESK - Customer Support
     * Quản lý tickets, support requests
     */
    HELPDESK(
            "HELPDESK",
            "Customer Support",
            "serp-helpdesk",
            "Support & Service",
            "headset",
            12,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.6.0",
            List.of("CRM")),

    /**
     * FIELD_SERVICE - Field Service Management
     * Quản lý dịch vụ field, maintenance
     */
    FIELD_SERVICE(
            "FIELD_SERVICE",
            "Field Service Management",
            "serp-field-service",
            "Support & Service",
            "map-marked-alt",
            13,
            ModuleType.SYSTEM,
            true,
            false,
            PricingModel.PER_USER,
            ModuleStatus.BETA,
            "0.4.0",
            List.of("HELPDESK"));

    // Fields
    private final String code;
    private final String moduleName;
    private final String keycloakClientId;
    private final String category;
    private final String icon;
    private final Integer displayOrder;
    private final ModuleType moduleType;
    private final Boolean isGlobal;
    private final Boolean isFree;
    private final PricingModel pricingModel;
    private final ModuleStatus status;
    private final String version;
    private final List<String> dependsOnModuleCodes;

    ModuleEnum(String code, String moduleName, String keycloakClientId,
            String category, String icon, Integer displayOrder,
            ModuleType moduleType, Boolean isGlobal, Boolean isFree,
            PricingModel pricingModel, ModuleStatus status, String version,
            List<String> dependsOnModuleCodes) {
        this.code = code;
        this.moduleName = moduleName;
        this.keycloakClientId = keycloakClientId;
        this.category = category;
        this.icon = icon;
        this.displayOrder = displayOrder;
        this.moduleType = moduleType;
        this.isGlobal = isGlobal;
        this.isFree = isFree;
        this.pricingModel = pricingModel;
        this.status = status;
        this.version = version;
        this.dependsOnModuleCodes = dependsOnModuleCodes;
    }

    public static ModuleEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(m -> m.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public boolean isFreeModule() {
        return Boolean.TRUE.equals(this.isFree);
    }

    public boolean isActive() {
        return this.status == ModuleStatus.ACTIVE;
    }

    public static List<ModuleEnum> getFreeModules() {
        return Arrays.stream(values())
                .filter(ModuleEnum::isFreeModule)
                .toList();
    }

    public static List<ModuleEnum> getModulesByCategory(String category) {
        return Arrays.stream(values())
                .filter(m -> m.getCategory().equals(category))
                .toList();
    }

    public static List<ModuleEnum> getActiveModules() {
        return Arrays.stream(values())
                .filter(ModuleEnum::isActive)
                .toList();
    }
}
