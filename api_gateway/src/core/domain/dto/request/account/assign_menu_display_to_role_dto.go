/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type AssignMenuDisplayToRoleDto struct {
	RoleId         int64   `json:"roleId"`
	MenuDisplayIds []int64 `json:"menuDisplayIds"`
}
