/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type AssignRoleToUserDto struct {
	UserId  int64   `json:"userId"`
	RoleIds []int64 `json:"roleIds"`
}
