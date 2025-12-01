package enum

type ScheduleTaskStatus string

const (
	ScheduleTaskPending       ScheduleTaskStatus = "PENDING"
	ScheduleTaskScheduled     ScheduleTaskStatus = "SCHEDULED"
	ScheduleTaskUnschedulable ScheduleTaskStatus = "UNSCHEDULABLE"
	ScheduleTaskPartial       ScheduleTaskStatus = "PARTIAL"
)
