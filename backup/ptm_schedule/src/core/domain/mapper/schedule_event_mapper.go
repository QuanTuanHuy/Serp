/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/request"
	"github.com/serp/ptm-schedule/src/core/domain/dto/response"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
)

// ToScheduleEventEntity converts request DTO to entity
func ToScheduleEventEntity(planID int64, req *request.ScheduleEventItemRequest) *entity.ScheduleEventEntity {
	return &entity.ScheduleEventEntity{
		BaseEntity: entity.BaseEntity{
			ID: req.ID,
		},
		SchedulePlanID: planID,
		ScheduleTaskID: req.ScheduleTaskID,
		DateMs:         req.DateMs,
		StartMin:       req.StartMin,
		EndMin:         req.EndMin,
		Status:         req.Status,
	}
}

// ToScheduleEventEntities converts request DTOs to entities
func ToScheduleEventEntities(planID int64, items []*request.ScheduleEventItemRequest) []*entity.ScheduleEventEntity {
	result := make([]*entity.ScheduleEventEntity, 0, len(items))
	for _, item := range items {
		result = append(result, ToScheduleEventEntity(planID, item))
	}
	return result
}

// ToScheduleEventResponse converts entity to response DTO
func ToScheduleEventResponse(e *entity.ScheduleEventEntity) *response.ScheduleEventResponse {
	return &response.ScheduleEventResponse{
		ID:             e.ID,
		SchedulePlanID: e.SchedulePlanID,
		ScheduleTaskID: e.ScheduleTaskID,
		DateMs:         e.DateMs,
		StartMin:       e.StartMin,
		EndMin:         e.EndMin,
		Status:         e.Status,
		ActualStartMin: e.ActualStartMin,
		ActualEndMin:   e.ActualEndMin,
		CreatedAt:      e.CreatedAt,
		UpdatedAt:      e.UpdatedAt,
	}
}

// ToScheduleEventResponses converts entities to response DTOs
func ToScheduleEventResponses(entities []*entity.ScheduleEventEntity) []*response.ScheduleEventResponse {
	result := make([]*response.ScheduleEventResponse, 0, len(entities))
	for _, e := range entities {
		result = append(result, ToScheduleEventResponse(e))
	}
	return result
}
