/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type RequestMoreModulesRequest struct {
	AdditionalModuleIds []int64 `json:"additionalModuleIds" validate:"required,min=1"`
}