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

// ToCalendarExceptionEntity converts request DTO to entity
func ToCalendarExceptionEntity(userID int64, req *request.CalendarExceptionItemRequest) *entity.CalendarExceptionEntity {
	return &entity.CalendarExceptionEntity{
		BaseEntity: entity.BaseEntity{
			ID: req.ID,
		},
		UserID:   userID,
		DateMs:   req.DateMs,
		StartMin: req.StartMin,
		EndMin:   req.EndMin,
		Type:     req.Type,
	}
}

// ToCalendarExceptionEntities converts request DTOs to entities
func ToCalendarExceptionEntities(userID int64, items []*request.CalendarExceptionItemRequest) []*entity.CalendarExceptionEntity {
	result := make([]*entity.CalendarExceptionEntity, 0, len(items))
	for _, item := range items {
		result = append(result, ToCalendarExceptionEntity(userID, item))
	}
	return result
}

// ToCalendarExceptionResponse converts entity to response DTO
func ToCalendarExceptionResponse(e *entity.CalendarExceptionEntity) *response.CalendarExceptionResponse {
	return &response.CalendarExceptionResponse{
		ID:        e.ID,
		UserID:    e.UserID,
		DateMs:    e.DateMs,
		StartMin:  e.StartMin,
		EndMin:    e.EndMin,
		Type:      e.Type,
		CreatedAt: e.CreatedAt,
		UpdatedAt: e.UpdatedAt,
	}
}

// ToCalendarExceptionResponses converts entities to response DTOs
func ToCalendarExceptionResponses(entities []*entity.CalendarExceptionEntity) []*response.CalendarExceptionResponse {
	result := make([]*response.CalendarExceptionResponse, 0, len(entities))
	for _, e := range entities {
		result = append(result, ToCalendarExceptionResponse(e))
	}
	return result
}
