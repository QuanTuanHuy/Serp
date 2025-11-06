/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/ptm-schedule/src/core/domain/dto/response"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
)

// ToScheduleWindowResponse converts entity to response DTO
func ToScheduleWindowResponse(e *entity.ScheduleWindowEntity) *response.ScheduleWindowResponse {
	return &response.ScheduleWindowResponse{
		ID:        e.ID,
		UserID:    e.UserID,
		DateMs:    e.DateMs,
		StartMin:  e.StartMin,
		EndMin:    e.EndMin,
		CreatedAt: e.CreatedAt,
		UpdatedAt: e.UpdatedAt,
	}
}

// ToScheduleWindowResponses converts entities to response DTOs
func ToScheduleWindowResponses(entities []*entity.ScheduleWindowEntity) []*response.ScheduleWindowResponse {
	result := make([]*response.ScheduleWindowResponse, 0, len(entities))
	for _, e := range entities {
		result = append(result, ToScheduleWindowResponse(e))
	}
	return result
}
