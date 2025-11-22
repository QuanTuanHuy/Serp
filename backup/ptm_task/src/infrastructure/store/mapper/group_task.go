/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"encoding/json"

	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
	"github.com/serp/ptm-task/src/kernel/utils"
)

func ToGroupTaskModel(groupTask *entity.GroupTaskEntity) *model.GroupTaskModel {
	if groupTask == nil {
		return nil
	}

	priorityBytes, _ := json.Marshal(groupTask.Priority)
	priorityStr := string(priorityBytes)

	return &model.GroupTaskModel{
		BaseModel: model.BaseModel{
			ID: groupTask.ID,
		},
		Title:          groupTask.Title,
		Description:    groupTask.Description,
		Priority:       priorityStr,
		Status:         string(groupTask.Status),
		TotalTasks:     groupTask.TotalTasks,
		CompletedTasks: groupTask.CompletedTasks,
		OrdinalNumber:  groupTask.OrdinalNumber,
		ActiveStatus:   string(groupTask.ActiveStatus),
		IsDefault:      groupTask.IsDefault,
		ProjectID:      groupTask.ProjectID,
	}
}

func ToGroupTaskEntity(groupTaskModel *model.GroupTaskModel) *entity.GroupTaskEntity {
	if groupTaskModel == nil {
		return nil
	}

	var priority []string
	if groupTaskModel.Priority != "" {
		json.Unmarshal([]byte(groupTaskModel.Priority), &priority)
	}

	return &entity.GroupTaskEntity{
		BaseEntity: entity.BaseEntity{
			ID:        groupTaskModel.ID,
			CreatedAt: groupTaskModel.CreatedAt.UnixMilli(),
			UpdatedAt: groupTaskModel.UpdatedAt.UnixMilli(),
		},
		Title:          groupTaskModel.Title,
		Description:    groupTaskModel.Description,
		Priority:       utils.ToPriorityEnum(priority),
		Status:         enum.Status(groupTaskModel.Status),
		TotalTasks:     groupTaskModel.TotalTasks,
		CompletedTasks: groupTaskModel.CompletedTasks,
		OrdinalNumber:  groupTaskModel.OrdinalNumber,
		ActiveStatus:   enum.ActiveStatus(groupTaskModel.ActiveStatus),
		IsDefault:      groupTaskModel.IsDefault,
		ProjectID:      groupTaskModel.ProjectID,
	}
}

func ToGroupTaskEntityList(groupTaskModels []*model.GroupTaskModel) []*entity.GroupTaskEntity {
	if groupTaskModels == nil {
		return nil
	}
	groupTasks := make([]*entity.GroupTaskEntity, len(groupTaskModels))
	for i, groupTaskModel := range groupTaskModels {
		groupTasks[i] = ToGroupTaskEntity(groupTaskModel)
	}
	return groupTasks
}

func ToGroupTaskModelList(groupTasks []*entity.GroupTaskEntity) []*model.GroupTaskModel {
	if groupTasks == nil {
		return nil
	}
	groupTaskModels := make([]*model.GroupTaskModel, len(groupTasks))
	for i, groupTask := range groupTasks {
		groupTaskModels[i] = ToGroupTaskModel(groupTask)
	}
	return groupTaskModels
}
