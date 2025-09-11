package request

type GetScheduleTaskRequest struct {
	TaskID         int64 `form:"taskId" validate:"omitempty,min=1"`
	ScheduleTaskID int64 `form:"scheduleTaskId" validate:"omitempty,min=1"`
}
