/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateRoleDto struct {
	Name             string  `json:"name"`
	Description      *string `json:"description"`
	IsRealmRole      *bool   `json:"isRealmRole"`
	KeycloakClientId *string `json:"keycloakClientId"`
	Priority         *int    `json:"priority"`
	Scope            *string `json:"scope"`
	ScopeId          *int64  `json:"scopeId"`
	ModuleId         *int64  `json:"moduleId"`
	OrganizationId   *int64  `json:"organizationId"`
	DepartmentId     *int64  `json:"departmentId"`
	ParentRoleId     *int64  `json:"parentRoleId"`
	RoleType         *string `json:"roleType"`
	IsDefault        *bool   `json:"isDefault"`

	PermissionIds []int64 `json:"permissionIds"`
}
