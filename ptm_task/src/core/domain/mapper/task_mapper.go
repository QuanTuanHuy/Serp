/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"time"

	"github.com/serp/ptm-task/src/core/domain/dto/message"
	"github.com/serp/ptm-task/src/core/domain/dto/request"
	"github.com/serp/ptm-task/src/core/domain/dto/response"
	"github.com/serp/ptm-task/src/core/domain/entity"
	"github.com/serp/ptm-task/src/core/port/store"
)

type TaskMapper struct{}

func NewTaskMapper() *TaskMapper {
	return &TaskMapper{}
}

func (m *TaskMapper) CreateRequestToEntity(req *request.CreateTaskRequest, userID, tenantID int64) *entity.TaskEntity {
	task := entity.NewTaskEntity()
	task.UserID = userID
	task.TenantID = tenantID
	task.Title = req.Title
	task.Description = req.Description
	task.Priority = req.Priority
	task.EstimatedDurationMin = req.EstimatedDurationMin
	task.PreferredStartDateMs = req.PreferredStartDateMs
	task.DeadlineMs = req.DeadlineMs
	task.EarliestStartMs = req.EarliestStartMs
	task.Category = req.Category

	if req.Tags != nil {
		task.Tags = req.Tags
	}

	task.ParentTaskID = req.ParentTaskID

	task.ProjectID = req.ProjectID
	task.IsRecurring = req.IsRecurring
	task.RecurrencePattern = req.RecurrencePattern
	task.RecurrenceConfig = req.RecurrenceConfig
	task.IsDeepWork = req.IsDeepWork
	task.IsMeeting = req.IsMeeting
	task.IsFlexible = req.IsFlexible
	task.ExternalID = req.ExternalID

	if req.Source != nil {
		task.Source = *req.Source
	}

	return task
}

func (m *TaskMapper) CreateTaskCreatedEvent(task *entity.TaskEntity) *message.TaskCreatedEvent {
	return &message.TaskCreatedEvent{
		TaskID:                task.ID,
		UserID:                task.UserID,
		TenantID:              task.TenantID,
		Title:                 task.Title,
		Priority:              task.Priority,
		EstimatedDurationMin:  task.EstimatedDurationMin,
		PreferredStartDateMs:  task.PreferredStartDateMs,
		DeadlineMs:            task.DeadlineMs,
		EarliestStartMs:       task.EarliestStartMs,
		Category:              task.Category,
		Tags:                  task.Tags,
		ParentTaskID:          task.ParentTaskID,
		HasSubtasks:           task.HasSubtasks,
		TotalSubtaskCount:     task.TotalSubtaskCount,
		CompletedSubtaskCount: task.CompletedSubtaskCount,
		IsDeepWork:            task.IsDeepWork,
		IsMeeting:             task.IsMeeting,
		IsFlexible:            task.IsFlexible,
		Status:                task.Status,
	}
}

func (m *TaskMapper) UpdateRequestToEntity(req *request.UpdateTaskRequest, existing *entity.TaskEntity) *entity.TaskEntity {
	if req.Title != nil {
		existing.Title = *req.Title
	}
	if req.Description != nil {
		existing.Description = req.Description
	}
	if req.Priority != nil {
		existing.Priority = *req.Priority
	}
	if req.PriorityScore != nil {
		existing.PriorityScore = req.PriorityScore
	}
	if req.EstimatedDurationMin != nil {
		existing.EstimatedDurationMin = req.EstimatedDurationMin
	}
	if req.ActualDurationMin != nil {
		existing.ActualDurationMin = req.ActualDurationMin
	}
	if req.PreferredStartDateMs != nil {
		existing.PreferredStartDateMs = req.PreferredStartDateMs
	}
	if req.DeadlineMs != nil {
		existing.DeadlineMs = req.DeadlineMs
	}
	if req.EarliestStartMs != nil {
		existing.EarliestStartMs = req.EarliestStartMs
	}
	if req.Category != nil {
		existing.Category = req.Category
	}
	if req.Tags != nil {
		existing.Tags = req.Tags
	}
	if req.ParentTaskID != nil {
		existing.ParentTaskID = req.ParentTaskID
	}
	if req.ProjectID != nil {
		existing.ProjectID = req.ProjectID
	}
	if req.IsRecurring != nil {
		existing.IsRecurring = *req.IsRecurring
	}
	if req.RecurrencePattern != nil {
		existing.RecurrencePattern = req.RecurrencePattern
	}
	if req.RecurrenceConfig != nil {
		existing.RecurrenceConfig = req.RecurrenceConfig
	}
	if req.IsDeepWork != nil {
		existing.IsDeepWork = *req.IsDeepWork
	}
	if req.IsMeeting != nil {
		existing.IsMeeting = *req.IsMeeting
	}
	if req.IsFlexible != nil {
		existing.IsFlexible = *req.IsFlexible
	}
	if req.Status != nil {
		existing.Status = *req.Status
	}

	return existing
}

func (m *TaskMapper) CreateTaskUpdatedEvent(task *entity.TaskEntity, req *request.UpdateTaskRequest) *message.TaskUpdatedEvent {
	return &message.TaskUpdatedEvent{
		TaskID:               task.ID,
		UserID:               task.UserID,
		TenantID:             task.TenantID,
		Title:                req.Title,
		Priority:             req.Priority,
		EstimatedDurationMin: req.EstimatedDurationMin,
		PreferredStartDateMs: req.PreferredStartDateMs,
		DeadlineMs:           req.DeadlineMs,
		EarliestStartMs:      req.EarliestStartMs,
		Category:             req.Category,
		Tags:                 req.Tags,
		ParentTaskID:         req.ParentTaskID,
		HasSubtasks:          &task.HasSubtasks,
		TotalSubtaskCount:    &task.TotalSubtaskCount,
		IsDeepWork:           req.IsDeepWork,
		IsMeeting:            req.IsMeeting,
		IsFlexible:           req.IsFlexible,
		Status:               req.Status,
	}
}

func (m *TaskMapper) EntityToResponse(task *entity.TaskEntity) *response.TaskResponse {
	resp := &response.TaskResponse{
		ID:                    task.ID,
		UserID:                task.UserID,
		TenantID:              task.TenantID,
		Title:                 task.Title,
		Description:           task.Description,
		Priority:              task.Priority,
		PriorityScore:         task.PriorityScore,
		EstimatedDurationMin:  task.EstimatedDurationMin,
		ActualDurationMin:     task.ActualDurationMin,
		IsDurationLearned:     task.IsDurationLearned,
		PreferredStartDateMs:  task.PreferredStartDateMs,
		DeadlineMs:            task.DeadlineMs,
		EarliestStartMs:       task.EarliestStartMs,
		Category:              task.Category,
		Tags:                  task.Tags,
		ParentTaskID:          task.ParentTaskID,
		ProjectID:             task.ProjectID,
		IsRecurring:           task.IsRecurring,
		RecurrencePattern:     task.RecurrencePattern,
		RecurrenceConfig:      task.RecurrenceConfig,
		ParentRecurringTaskID: task.ParentRecurringTaskID,
		IsDeepWork:            task.IsDeepWork,
		IsMeeting:             task.IsMeeting,
		IsFlexible:            task.IsFlexible,
		Status:                task.Status,
		ActiveStatus:          task.ActiveStatus,
		ExternalID:            task.ExternalID,
		Source:                task.Source,
		CompletedAt:           task.CompletedAt,
		CreatedAt:             task.CreatedAt,
		UpdatedAt:             task.UpdatedAt,
	}

	// Compute dynamic fields
	currentTimeMs := time.Now().UnixMilli()
	isOverdue := task.IsOverdue(currentTimeMs)
	canBeScheduled := task.CanBeScheduled(currentTimeMs)
	resp.IsOverdue = &isOverdue
	resp.CanBeScheduled = &canBeScheduled

	if task.DeadlineMs != nil {
		remainingMs := *task.DeadlineMs - currentTimeMs
		resp.DeadlineRemainingMs = &remainingMs
	}

	return resp
}

func (m *TaskMapper) EntitiesToResponses(tasks []*entity.TaskEntity) []*response.TaskResponse {
	responses := make([]*response.TaskResponse, 0, len(tasks))
	for _, task := range tasks {
		responses = append(responses, m.EntityToResponse(task))
	}
	return responses
}

func (m *TaskMapper) EntitiesToTreeResponses(task *entity.TaskEntity) *response.TaskResponse {
	resp := m.EntityToResponse(task)
	if len(task.SubTasks) > 0 {
		resp.SubTasks = make([]*response.TaskResponse, 0, len(task.SubTasks))
		for _, subTask := range task.SubTasks {
			resp.SubTasks = append(resp.SubTasks, m.EntitiesToTreeResponses(subTask))
		}
	}
	return resp
}

func (m *TaskMapper) FilterMapper(req *request.TaskFilterRequest) *store.TaskFilter {
	filter := store.NewTaskFilter()

	if req.Status != nil {
		filter.Statuses = append(filter.Statuses, *req.Status)
	}
	if req.Priority != nil {
		filter.Priorities = append(filter.Priorities, *req.Priority)
	}
	if req.ProjectID != nil {
		filter.ProjectID = req.ProjectID
	}
	if req.ParentTaskID != nil {
		filter.ParentTaskID = req.ParentTaskID
	}
	if req.Category != nil {
		filter.Categories = append(filter.Categories, *req.Category)
	}
	if len(req.Tags) > 0 {
		filter.Tags = req.Tags
	}
	if req.IsDeepWork != nil {
		filter.IsDeepWork = req.IsDeepWork
	}
	if req.IsMeeting != nil {
		filter.IsMeeting = req.IsMeeting
	}
	if req.IsRecurring != nil {
		filter.IsRecurring = req.IsRecurring
	}
	if req.DeadlineFrom != nil {
		filter.DeadlineFrom = req.DeadlineFrom
	}
	if req.DeadlineTo != nil {
		filter.DeadlineTo = req.DeadlineTo
	}

	filter.Limit = req.PageSize
	filter.Offset = req.Page * req.PageSize
	filter.SortBy = req.SortBy
	filter.SortOrder = req.SortOrder

	return filter
}
