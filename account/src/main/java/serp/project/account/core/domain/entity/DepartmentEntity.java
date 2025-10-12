/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Quản lý phòng ban trong organization
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class DepartmentEntity extends BaseEntity {
    private Long organizationId;

    private String name;

    private String code;

    private String description;

    private Long parentDepartmentId;

    private Long managerId;

    /**
     * Danh sách module IDs mà department này có quyền truy cập mặc định
     * Khi user join vào department, tự động được access các modules này
     */
    private List<Long> defaultModuleIds;

    /**
     * Danh sách role IDs mặc định cho members của department
     * Ví dụ: Sales Department → default role "Sales Person"
     */
    private List<Long> defaultRoleIds;

    /**
     * Trạng thái active
     */
    private Boolean isActive;
}
