/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type AddModuleToPlanRequest struct {
	ModuleId          int64  `json:"moduleId"`
	AccessLevel       string `json:"accessLevel"`
	IsDefault         *bool  `json:"isDefault"`
	MaxUsersPerModule *int   `json:"maxUsersPerModule"`
}
