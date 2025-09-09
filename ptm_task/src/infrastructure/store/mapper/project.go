/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/domain/enum"
	"github.com/serp/ptm-task/src/infrastructure/store/model"
)

func ToProjectModel(project *entity.ProjectEntity) *model.ProjectModel {
	if project == nil {
		return nil
	}
	return &model.ProjectModel{
		BaseModel: model.BaseModel{
			ID: project.ID,
		},
		Name:         project.Name,
		Description:  project.Description,
		Color:        project.Color,
		Status:       string(project.Status),
		ActiveStatus: string(project.ActiveStatus),
		IsDefault:    project.IsDefault,
		OwnerID:      project.OwnerID,
	}
}

func ToProjectEntity(projectModel *model.ProjectModel) *entity.ProjectEntity {
	if projectModel == nil {
		return nil
	}
	return &entity.ProjectEntity{
		BaseEntity: entity.BaseEntity{
			ID:        projectModel.ID,
			CreatedAt: projectModel.CreatedAt.UnixMilli(),
			UpdatedAt: projectModel.UpdatedAt.UnixMilli(),
		},
		Name:         projectModel.Name,
		Description:  projectModel.Description,
		Color:        projectModel.Color,
		Status:       enum.Status(projectModel.Status),
		ActiveStatus: enum.ActiveStatus(projectModel.ActiveStatus),
		IsDefault:    projectModel.IsDefault,
		OwnerID:      projectModel.OwnerID,
	}
}

func ToProjectEntityList(projectModels []*model.ProjectModel) []*entity.ProjectEntity {
	if projectModels == nil {
		return nil
	}
	projects := make([]*entity.ProjectEntity, len(projectModels))
	for i, projectModel := range projectModels {
		projects[i] = ToProjectEntity(projectModel)
	}
	return projects
}
