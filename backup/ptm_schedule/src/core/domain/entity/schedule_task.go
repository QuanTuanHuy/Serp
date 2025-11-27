package entity

import "github.com/serp/ptm-schedule/src/core/domain/enum"

type ScheduleTaskEntity struct {
	BaseEntity
	TaskID               int64             `json:"taskId"`
	Title                string            `json:"title"`
	Priority             []enum.Priority   `json:"priority"`
	Status               enum.Status       `json:"status"`
	StartDate            int64             `json:"startDate"`
	Deadline             int64             `json:"deadline"`
	Duration             float64           `json:"duration"`
	ActiveStatus         enum.ActiveStatus `json:"activeStatus"`
	PreferenceLevel      int32             `json:"preferenceLevel"`
	IsSynchronizedWithWO bool              `json:"isSynchronizedWithWO"`
	TaskOrder            int32             `json:"taskOrder"`
	Weight               float64           `json:"weight"`
	StopTime             float64           `json:"stopTime"`
	TaskBatch            int32             `json:"taskBatch"`
	SchedulePlanID       int64             `json:"schedulePlanId"`
	Repeat               enum.RepeatLevel  `json:"repeat"`
	ScheduleGroupID      int64             `json:"scheduleGroupId"`
}

func (st *ScheduleTaskEntity) MarkAsSynchronized() {
	st.IsSynchronizedWithWO = true
}

func (st *ScheduleTaskEntity) UpdateOptimizeTask(taskOrder int32, weight, stopTime float64) {
	st.TaskOrder = taskOrder
	st.Weight = weight
	st.StopTime = stopTime
}
