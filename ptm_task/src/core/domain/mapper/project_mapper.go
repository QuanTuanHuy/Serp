/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/kernel/utils"
)

func CreateProjectMapper(createProject *request.CreateProjectDTO, userID int64) *entity.ProjectEntity {
	return &entity.ProjectEntity{
		Name:         createProject.Name,
		Description:  utils.StringValueWithDefault(createProject.Description, ""),
		Color:        utils.StringValueWithDefault(createProject.Color, "indigo"),
		Status:       enum.Status(utils.StringValueWithDefault(createProject.Status, string(enum.ToDo))),
		ActiveStatus: enum.ActiveStatus(utils.StringValueWithDefault(createProject.ActiveStatus, string(enum.Active))),
		IsDefault:    createProject.IsDefault,
		OwnerID:      userID,
	}
}
