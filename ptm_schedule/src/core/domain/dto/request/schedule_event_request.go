/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type ScheduleEventItemRequest struct {
	ID             int64                    `json:"id"`
	ScheduleTaskID int64                    `json:"scheduleTaskId" binding:"required,gt=0"`
	DateMs         int64                    `json:"dateMs" binding:"required,gt=0"`
	StartMin       int                      `json:"startMin" binding:"required,gte=0,lte=1440"`
	EndMin         int                      `json:"endMin" binding:"required,gte=0,lte=1440"`
	Status         enum.ScheduleEventStatus `json:"status"`
}

type SaveEventsRequest struct {
	SchedulePlanID int64                       `json:"schedulePlanId" binding:"required,gt=0"`
	Events         []*ScheduleEventItemRequest `json:"events" binding:"required,dive"`
}

type UpdateEventStatusRequest struct {
	Status         enum.ScheduleEventStatus `json:"status" binding:"required"`
	ActualStartMin *int                     `json:"actualStartMin"`
	ActualEndMin   *int                     `json:"actualEndMin"`
}

type MoveEventRequest struct {
	NewDateMs   int64 `json:"newDateMs" binding:"required,gt=0"`
	NewStartMin int   `json:"newStartMin" binding:"required,gte=0,lte=1440"`
	NewEndMin   int   `json:"newEndMin" binding:"required,gte=0,lte=1440"`
}

type CompleteEventRequest struct {
	ActualStartMin int `json:"actualStartMin" binding:"required,gte=0,lte=1440"`
	ActualEndMin   int `json:"actualEndMin" binding:"required,gte=0,lte=1440"`
}

type SplitEventRequest struct {
	SplitPointMin int `json:"splitPointMin" binding:"required,gte=0,lte=1440"`
}
