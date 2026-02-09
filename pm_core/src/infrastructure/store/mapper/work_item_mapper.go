/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/infrastructure/store/model"
)

type WorkItemMapper struct{}

func NewWorkItemMapper() *WorkItemMapper {
	return &WorkItemMapper{}
}

func (m *WorkItemMapper) ToEntity(mdl *model.WorkItemModel) *entity.WorkItemEntity {
	if mdl == nil {
		return nil
	}

	return &entity.WorkItemEntity{
		BaseEntity: entity.BaseEntity{
			ID:        mdl.ID,
			CreatedAt: mdl.CreatedAt.UnixMilli(),
			UpdatedAt: mdl.UpdatedAt.UnixMilli(),
		},
		ProjectID:           mdl.ProjectID,
		ItemNumber:          mdl.ItemNumber,
		Title:               mdl.Title,
		Description:         mdl.Description,
		Type:                mdl.Type,
		Status:              mdl.Status,
		Priority:            mdl.Priority,
		ParentID:            mdl.ParentID,
		SprintID:            mdl.SprintID,
		MilestoneID:         mdl.MilestoneID,
		BoardColumnID:       mdl.BoardColumnID,
		ReporterID:          mdl.ReporterID,
		StoryPoints:         mdl.StoryPoints,
		EstimatedHours:      mdl.EstimatedHours,
		StartDateMs:         mdl.StartDateMs,
		DueDateMs:           mdl.DueDateMs,
		ActiveStatus:        mdl.ActiveStatus,
		Position:            mdl.Position,
		HasChildren:         mdl.HasChildren,
		ChildCount:          mdl.ChildCount,
		CompletedChildCount: mdl.CompletedChildCount,
		CommentCount:        mdl.CommentCount,
	}
}

func (m *WorkItemMapper) ToModel(e *entity.WorkItemEntity) *model.WorkItemModel {
	if e == nil {
		return nil
	}

	return &model.WorkItemModel{
		BaseModel: model.BaseModel{
			ID: e.ID,
		},
		ProjectID:           e.ProjectID,
		ItemNumber:          e.ItemNumber,
		Title:               e.Title,
		Description:         e.Description,
		Type:                e.Type,
		Status:              e.Status,
		Priority:            e.Priority,
		ParentID:            e.ParentID,
		SprintID:            e.SprintID,
		MilestoneID:         e.MilestoneID,
		BoardColumnID:       e.BoardColumnID,
		ReporterID:          e.ReporterID,
		StoryPoints:         e.StoryPoints,
		EstimatedHours:      e.EstimatedHours,
		StartDateMs:         e.StartDateMs,
		DueDateMs:           e.DueDateMs,
		ActiveStatus:        e.ActiveStatus,
		Position:            e.Position,
		HasChildren:         e.HasChildren,
		ChildCount:          e.ChildCount,
		CompletedChildCount: e.CompletedChildCount,
		CommentCount:        e.CommentCount,
	}
}

func (m *WorkItemMapper) ToEntities(models []*model.WorkItemModel) []*entity.WorkItemEntity {
	if models == nil {
		return nil
	}

	entities := make([]*entity.WorkItemEntity, 0, len(models))
	for _, mdl := range models {
		if e := m.ToEntity(mdl); e != nil {
			entities = append(entities, e)
		}
	}

	return entities
}
