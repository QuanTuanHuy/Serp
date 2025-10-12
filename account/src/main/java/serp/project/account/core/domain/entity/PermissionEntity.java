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
import serp.project.account.core.domain.enums.PermissionAction;
import serp.project.account.core.domain.enums.PermissionScope;
import serp.project.account.core.domain.enums.PermissionType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class PermissionEntity extends BaseEntity {
    /**
     * Tên permission (unique identifier)
     * Ví dụ: "customer.create", "lead.read", "invoice.approve"
     */
    private String name;

    private String description;

    /**
     * Resource/Entity mà permission áp dụng
     * Ví dụ: "customer", "lead", "invoice", "user"
     */
    private String resource;

    /**
     * Action/Operation
     * CREATE, READ, UPDATE, DELETE (CRUD)
     * Hoặc custom actions: APPROVE, REJECT, EXPORT, IMPORT, SHARE
     */
    private PermissionAction action;

    /**
     * ID của module mà permission này thuộc về
     * null = global permission (system-wide)
     */
    private Long moduleId;

    private PermissionScope scope;

    /**
     * Điều kiện bổ sung
     * JSON string chứa conditions
     * Ví dụ: {"department_id": "${user.department_id}", "status": "active"}
     * null = không có điều kiện bổ sung
     */
    private String conditions;

    private PermissionType permissionType;
}
