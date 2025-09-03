/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateProjectDTO struct {
	Name         string  `json:"name" validate:"required,min=1,max=200"`
	Description  *string `json:"description" validate:"omitempty,max=500"`
	Color        *string `json:"color" validate:"omitempty,max=100"`
	Status       *string `json:"status" validate:"omitempty,oneof=TODO IN_PROGRESS DONE PENDING"`
	ActiveStatus *string `json:"activeStatus" validate:"omitempty,oneof=ACTIVE INACTIVE"`
	IsDefault    bool    `json:"-"`
}
