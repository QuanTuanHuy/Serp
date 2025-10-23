/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type PlanModuleDto struct {
	ModuleId          int64  `json:"moduleId" binding:"required"`
	IsIncluded        bool   `json:"isIncluded"`
	LicenseType       string `json:"licenseType" binding:"required"`
	MaxUsersPerModule *int   `json:"maxUsersPerModule" binding:"omitempty,gte=1"`
}
