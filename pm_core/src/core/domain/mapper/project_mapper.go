/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/pm-core/src/core/domain/dto/request"
	"github.com/serp/pm-core/src/core/domain/dto/response"
	"github.com/serp/pm-core/src/core/domain/entity"
	"github.com/serp/pm-core/src/core/domain/enum"
	"github.com/serp/pm-core/src/core/port/store"
)

type ProjectMapper struct{}

func NewProjectMapper() *ProjectMapper {
	return &ProjectMapper{}
}

func (m *ProjectMapper) CreateRequestToEntity(req *request.CreateProjectRequest, ownerID, tenantID int64) *entity.ProjectEntity {
	project := entity.NewProjectEntity()
	project.TenantID = tenantID
	project.Name = req.Name
	project.Key = req.Key
	project.Description = req.Description

	if req.Visibility != nil {
		project.Visibility = *req.Visibility
	}
	if req.StartDateMs != nil {
		project.StartDateMs = req.StartDateMs
	}
	if req.TargetEndDateMs != nil {
		project.TargetEndDateMs = req.TargetEndDateMs
	}

	return project
}

func (m *ProjectMapper) UpdateRequestToEntity(req *request.UpdateProjectRequest, existing *entity.ProjectEntity) *entity.ProjectEntity {
	if req.Name != nil {
		existing.Name = *req.Name
	}
	if req.Description != nil {
		existing.Description = req.Description
	}
	if req.Status != nil {
		existing.Status = *req.Status
	}
	if req.Visibility != nil {
		existing.Visibility = *req.Visibility
	}
	if req.StartDateMs != nil {
		existing.StartDateMs = req.StartDateMs
	}
	if req.TargetEndDateMs != nil {
		existing.TargetEndDateMs = req.TargetEndDateMs
	}
	return existing
}

func (m *ProjectMapper) FilterRequestToFilter(req *request.ProjectFilterRequest) *store.ProjectFilter {
	filter := store.NewProjectFilter()
	filter.Statuses = req.Statuses
	filter.Visibility = req.Visibility

	activeStatus := string(enum.Active)
	filter.ActiveStatus = &activeStatus

	filter.SortBy = req.SortBy
	filter.SortOrder = req.SortOrder
	filter.Limit = req.PageSize
	filter.Offset = req.Page * req.PageSize

	return filter
}

func (m *ProjectMapper) EntityToResponse(e *entity.ProjectEntity) *response.ProjectResponse {
	if e == nil {
		return nil
	}
	return &response.ProjectResponse{
		ID:                 e.ID,
		TenantID:           e.TenantID,
		Name:               e.Name,
		Key:                e.Key,
		Description:        e.Description,
		Status:             e.Status,
		Visibility:         e.Visibility,
		StartDateMs:        e.StartDateMs,
		TargetEndDateMs:    e.TargetEndDateMs,
		NextItemNumber:     e.NextItemNumber,
		TotalWorkItems:     e.TotalWorkItems,
		CompletedWorkItems: e.CompletedWorkItems,
		CreatedAt:          e.CreatedAt,
		UpdatedAt:          e.UpdatedAt,
	}
}

func (m *ProjectMapper) EntitiesToResponses(entities []*entity.ProjectEntity) []*response.ProjectResponse {
	if entities == nil {
		return nil
	}
	responses := make([]*response.ProjectResponse, 0, len(entities))
	for _, e := range entities {
		if r := m.EntityToResponse(e); r != nil {
			responses = append(responses, r)
		}
	}
	return responses
}
