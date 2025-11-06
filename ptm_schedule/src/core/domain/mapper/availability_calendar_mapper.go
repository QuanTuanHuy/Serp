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

func ToAvailabilityCalendarEntity(userID int64, req *request.AvailabilityCalendarItemRequest) *entity.AvailabilityCalendarEntity {
	return &entity.AvailabilityCalendarEntity{
		BaseEntity: entity.BaseEntity{
			ID: req.ID,
		},
		UserID:       userID,
		DayOfWeek:    req.DayOfWeek,
		StartMin:     req.StartMin,
		EndMin:       req.EndMin,
		ActiveStatus: req.ActiveStatus,
	}
}

func ToAvailabilityCalendarEntities(userID int64, items []*request.AvailabilityCalendarItemRequest) []*entity.AvailabilityCalendarEntity {
	result := make([]*entity.AvailabilityCalendarEntity, 0, len(items))
	for _, item := range items {
		result = append(result, ToAvailabilityCalendarEntity(userID, item))
	}
	return result
}

func ToAvailabilityCalendarResponse(e *entity.AvailabilityCalendarEntity) *response.AvailabilityCalendarResponse {
	return &response.AvailabilityCalendarResponse{
		ID:           e.ID,
		UserID:       e.UserID,
		DayOfWeek:    e.DayOfWeek,
		StartMin:     e.StartMin,
		EndMin:       e.EndMin,
		ActiveStatus: e.ActiveStatus,
		CreatedAt:    e.CreatedAt,
		UpdatedAt:    e.UpdatedAt,
	}
}

func ToAvailabilityCalendarResponses(entities []*entity.AvailabilityCalendarEntity) []*response.AvailabilityCalendarResponse {
	result := make([]*response.AvailabilityCalendarResponse, 0, len(entities))
	for _, e := range entities {
		result = append(result, ToAvailabilityCalendarResponse(e))
	}
	return result
}
