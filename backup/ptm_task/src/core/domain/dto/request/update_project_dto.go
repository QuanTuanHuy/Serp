/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
)

type UpdateProjectDTO struct {
	Name         *string `json:"name" validate:"omitempty,min=1,max=200"`
	Description  *string `json:"description" validate:"omitempty,max=500"`
	Color        *string `json:"color" validate:"omitempty"`
	Status       *string `json:"status" validate:"omitempty,oneof=TODO IN_PROGRESS DONE PENDING"`
	ActiveStatus *string `json:"activeStatus" validate:"omitempty,oneof=ACTIVE INACTIVE"`
}

func (u UpdateProjectDTO) ToProjectEntity(project *entity.ProjectEntity) *entity.ProjectEntity {
	if u.Name != nil {
		project.Name = *u.Name
	}
	if u.Description != nil {
		project.Description = *u.Description
	}
	if u.Color != nil {
		project.Color = *u.Color
	}
	if u.Status != nil {
		project.Status = enum.Status(*u.Status)
	}
	if u.ActiveStatus != nil {
		project.ActiveStatus = enum.ActiveStatus(*u.ActiveStatus)
	}
	return project
}
