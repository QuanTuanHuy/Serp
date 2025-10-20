/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateRoleDto struct {
	Name        string `json:"name"`
	Code        string `json:"code"`
	Description string `json:"description"`
	Level       string `json:"level"`
	ModuleId    *int64 `json:"moduleId"`
}
