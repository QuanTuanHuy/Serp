package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type SchedulePlanEntity struct {
	BaseEntity
	UserID            int64             `json:"userId"`
	StartDate         int64             `json:"startDate"`
	EndDate           *int64            `json:"endDate"`
	ActiveStatus      enum.ActiveStatus `json:"activeStatus"`
	ActiveTaskBatch   int32             `json:"activeTaskBatch"`
	IsActiveTaskBatch bool              `json:"isActiveTaskBatch"`
}

func (sp *SchedulePlanEntity) UpdateTaskBatch(activeTaskBatch int32, isActiveTaskBatch bool) {
	sp.ActiveTaskBatch = activeTaskBatch
	sp.IsActiveTaskBatch = isActiveTaskBatch
}
