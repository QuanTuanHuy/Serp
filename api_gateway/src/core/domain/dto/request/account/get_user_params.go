/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type GetUserParams struct {
	Page           *int    `json:"page,omitempty"`
	PageSize       *int    `json:"pageSize,omitempty"`
	SortBy         *string `json:"sortBy,omitempty"`
	SortDir        *string `json:"sortDir,omitempty"`
	Search         *string `json:"search,omitempty"`
	OrganizationID *int64  `json:"organizationId,omitempty"`
	Status         *string `json:"status,omitempty"`
	UserType       *string `json:"userType,omitempty"`
	RoleId         *int64  `json:"roleId,omitempty"`
	DepartmentId   *int64  `json:"departmentId,omitempty"`
}
