/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type TagAttachDTO struct {
	ResourceType string `json:"resourceType" validate:"required,oneof=PROJECT TASK NOTE"`
	ResourceID   int64  `json:"resourceId" validate:"required,gt=0"`
}

type TagAttachBatchDTO struct {
	ResourceType string  `json:"resourceType" validate:"required,oneof=PROJECT TASK NOTE"`
	ResourceIDs  []int64 `json:"resourceIds" validate:"required,min=1,dive,gt=0"`
}
