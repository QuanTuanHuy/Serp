/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreatePermissionDto struct {
	Name        string `json:"name"`
	Code        string `json:"code"`
	Description string `json:"description"`
	ModuleId    int64  `json:"moduleId"`
}
