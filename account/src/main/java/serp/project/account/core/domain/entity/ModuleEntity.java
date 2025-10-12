/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.ModuleType;
import serp.project.account.core.domain.enums.PricingModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho một Module/App trong hệ thống (CRM, PTM, Accounting, etc.)
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ModuleEntity extends BaseEntity {

    private String moduleName;

    /**
     * Mã định danh unique của module (CRM, PTM, ACCOUNTING)
     */
    private String code;

    private String description;

    /**
     * Keycloak Client ID cho module này
     */
    private String keycloakClientId;

    /**
     * Danh mục module (Sales, Productivity, Finance, HR, etc.)
     * Dùng để nhóm modules trong UI
     */
    private String category;

    /**
     * Icon cho module (FontAwesome, Material Icons, etc.)
     */
    private String icon;

    /**
     * Thứ tự hiển thị trong UI
     */
    private Integer displayOrder;

    private ModuleType moduleType;

    /**
     * Module có sẵn cho tất cả organizations không
     * true = global module (CRM, PTM)
     * false = custom module cho 1 org cụ thể
     */
    @Builder.Default
    private Boolean isGlobal = true;

    /**
     * ID của organization sở hữu module (nếu là custom module)
     * null nếu isGlobal = true
     */
    private Long organizationId;

    private Boolean isFree;

    /**
     * Mô hình tính phí
     */
    private PricingModel pricingModel;

    /**
     * Danh sách module IDs mà module này phụ thuộc vào
     * Ví dụ: Invoicing module requires Sales module
     */
    @Builder.Default
    private List<Long> dependsOnModuleIds = new ArrayList<>();

    private ModuleStatus status;

    private String version;
}
