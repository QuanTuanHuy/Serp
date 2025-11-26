/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type AssignUserToModuleRequest struct {
	UserId   int64 `json:"userId"`
	ModuleId int64 `json:"moduleId"`
	RoleID   int64 `json:"roleId"`
}
