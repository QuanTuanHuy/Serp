/*
Author: QuanTuanHuy
Description: Part of Serp Project - Schedule Plan DTOs
*/

package response

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type ScheduleEventResponse struct {
	ID             int64                    `json:"id"`
	SchedulePlanID int64                    `json:"schedulePlanId"`
	ScheduleTaskID int64                    `json:"scheduleTaskId"`
	DateMs         int64                    `json:"dateMs"`
	StartMin       int                      `json:"startMin"`
	EndMin         int                      `json:"endMin"`
	Status         enum.ScheduleEventStatus `json:"status"`
	ActualStartMin *int                     `json:"actualStartMin"`
	ActualEndMin   *int                     `json:"actualEndMin"`
	CreatedAt      int64                    `json:"createdAt"`
	UpdatedAt      int64                    `json:"updatedAt"`
}
