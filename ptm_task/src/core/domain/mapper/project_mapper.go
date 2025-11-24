/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"time"

	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/dto/response"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
)

type ProjectMapper struct{}

func NewProjectMapper() *ProjectMapper {
	return &ProjectMapper{}
}

func (m *ProjectMapper) CreateRequestToEntity(req *request.CreateProjectRequest, userID, tenantID int64) *entity.ProjectEntity {
	project := entity.NewProjectEntity()
	project.UserID = userID
	project.TenantID = tenantID
	project.Title = req.Title
	project.Description = req.Description
	project.Priority = req.Priority
	project.StartDateMs = req.StartDateMs
	project.DeadlineMs = req.DeadlineMs
	if req.Color != nil {
		project.Color = req.Color
	}
	project.Icon = req.Icon

	if req.IsFavorite != nil {
		project.IsFavorite = *req.IsFavorite
	}

	return project
}

func (m *ProjectMapper) UpdateRequestToEntity(req *request.UpdateProjectRequest, existing *entity.ProjectEntity) *entity.ProjectEntity {
	if req.Title != nil {
		existing.Title = *req.Title
	}
	if req.Description != nil {
		existing.Description = req.Description
	}
	if req.Priority != nil {
		existing.Priority = *req.Priority
	}
	if req.Status != nil {
		existing.Status = *req.Status
	}
	if req.StartDateMs != nil {
		existing.StartDateMs = req.StartDateMs
	}
	if req.DeadlineMs != nil {
		existing.DeadlineMs = req.DeadlineMs
	}
	if req.Color != nil {
		existing.Color = req.Color
	}
	if req.Icon != nil {
		existing.Icon = req.Icon
	}
	if req.IsFavorite != nil {
		existing.IsFavorite = *req.IsFavorite
	}

	return existing
}

func (m *ProjectMapper) EntityToResponse(project *entity.ProjectEntity, includeStats bool) *response.ProjectResponse {
	resp := &response.ProjectResponse{
		ID:                 project.ID,
		UserID:             project.UserID,
		TenantID:           project.TenantID,
		Title:              project.Title,
		Description:        project.Description,
		Status:             project.Status,
		Priority:           project.Priority,
		StartDateMs:        project.StartDateMs,
		DeadlineMs:         project.DeadlineMs,
		ProgressPercentage: project.ProgressPercentage,
		Color:              project.Color,
		Icon:               project.Icon,
		IsFavorite:         project.IsFavorite,
		CreatedAt:          project.CreatedAt,
		UpdatedAt:          project.UpdatedAt,
	}

	if includeStats {
		resp.TotalTasks = &project.TotalTasks
		resp.CompletedTasks = &project.CompletedTasks
		resp.EstimatedHours = &project.EstimatedHours
		resp.ActualHours = &project.ActualHours
	}

	currentTimeMs := time.Now().UnixMilli()
	isOverdue := project.IsOverdue(currentTimeMs)
	resp.IsOverdue = &isOverdue
	resp.DeadlineRemainingMs = project.GetDeadlineRemainingMs(currentTimeMs)

	return resp
}

func (m *ProjectMapper) EntitiesToResponses(projects []*entity.ProjectEntity, includeStats bool) []*response.ProjectResponse {
	responses := make([]*response.ProjectResponse, 0, len(projects))
	for _, project := range projects {
		responses = append(responses, m.EntityToResponse(project, includeStats))
	}
	return responses
}

func (m *ProjectMapper) EntityToStatsResponse(stats *entity.ProjectEntity) *response.ProjectStatsResponse {
	return &response.ProjectStatsResponse{
		ProjectID:            stats.ID,
		TotalTasks:           stats.TotalTasks,
		CompletedTasks:       stats.CompletedTasks,
		TodoTasks:            stats.TotalTasks - stats.CompletedTasks,
		InProgressTasks:      0, // TODO: Calculate from task statuses
		EstimatedDurationMin: int(stats.EstimatedHours * 60),
		ActualDurationMin:    int(stats.ActualHours * 60),
		EstimatedHours:       stats.EstimatedHours,
		ActualHours:          stats.ActualHours,
		OverdueTasks:         0, // TODO: Calculate from tasks
		ProgressPercentage:   stats.ProgressPercentage,
	}
}

func (m *ProjectMapper) FilterMapper(req *request.ProjectFilterRequest) *store.ProjectFilter {
	filter := store.NewProjectFilter()
	if req.Status != nil {
		filter.Statuses = append(filter.Statuses, *req.Status)
	}
	if req.Priority != nil {
		filter.Priorities = append(filter.Priorities, *req.Priority)
	}
	filter.Limit = req.PageSize
	filter.Offset = req.Page * req.PageSize
	filter.SortBy = req.SortBy
	filter.SortOrder = req.SortOrder
	return filter
}
