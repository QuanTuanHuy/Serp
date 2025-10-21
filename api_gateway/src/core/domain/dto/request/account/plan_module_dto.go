/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type PlanModuleDto struct {
	ModuleId          int64  `json:"moduleId"`
	IsIncluded        *bool  `json:"isIncluded"`
	LicenseType       string `json:"licenseType"`
	MaxUsersPerModule *int   `json:"maxUsersPerModule"`
}
