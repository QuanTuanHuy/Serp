/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type MilestoneMapper struct{}

func NewMilestoneMapper() *MilestoneMapper {
	return &MilestoneMapper{}
}

func (m *MilestoneMapper) ToEntity(mdl *model.MilestoneModel) *entity.MilestoneEntity {
	if mdl == nil {
		return nil
	}

	return &entity.MilestoneEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID:          mdl.ProjectID,
		Name:               mdl.Name,
		Description:        mdl.Description,
		Status:             mdl.Status,
		TargetDateMs:       mdl.TargetDateMs,
		TotalWorkItems:     mdl.TotalWorkItems,
		CompletedWorkItems: mdl.CompletedWorkItems,
		ProgressPercentage: mdl.ProgressPercentage,
		ActiveStatus:       mdl.ActiveStatus,
	}
}

func (m *MilestoneMapper) ToModel(e *entity.MilestoneEntity) *model.MilestoneModel {
	if e == nil {
		return nil
	}

	return &model.MilestoneModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID:          e.ProjectID,
		Name:               e.Name,
		Description:        e.Description,
		Status:             e.Status,
		TargetDateMs:       e.TargetDateMs,
		TotalWorkItems:     e.TotalWorkItems,
		CompletedWorkItems: e.CompletedWorkItems,
		ProgressPercentage: e.ProgressPercentage,
		ActiveStatus:       e.ActiveStatus,
	}
}

func (m *MilestoneMapper) ToEntities(models []*model.MilestoneModel) []*entity.MilestoneEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.MilestoneEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
