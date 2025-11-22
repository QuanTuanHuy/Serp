/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type SetParentTaskDTO struct {
	ParentTaskID *int64 `json:"parentTaskId" validate:"omitempty,min=1"`
}
