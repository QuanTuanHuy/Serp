/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type BulkAssignUsersRequest struct {
	OrganizationId int64   `json:"organizationId"`
	UserIds        []int64 `json:"userIds"`
	ModuleId       int64   `json:"moduleId"`
}
