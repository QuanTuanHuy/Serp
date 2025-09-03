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

func CreateGroupTaskMapper(createGroupTask *request.CreateGroupTaskDTO) *entity.GroupTaskEntity {
	priorities := utils.StringSliceFromPointers(createGroupTask.Priority)

	description := utils.StringValue(createGroupTask.Description)
	status := utils.StringValueWithDefault(createGroupTask.Status, string(enum.ToDo))

	return &entity.GroupTaskEntity{
		Title:        createGroupTask.Title,
		Description:  description,
		Priority:     utils.ToPriorityEnum(priorities),
		Status:       enum.Status(status),
		ActiveStatus: enum.Active,
		IsDefault:    false,
		ProjectID:    createGroupTask.ProjectID,
	}
}
