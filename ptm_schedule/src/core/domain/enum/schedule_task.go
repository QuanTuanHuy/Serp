package enum

type ScheduleTaskStatus string

const (
	ScheduleTaskStatusPending       ScheduleTaskStatus = "PENDING"
	ScheduleTaskStatusScheduled     ScheduleTaskStatus = "SCHEDULED"
	ScheduleTaskStatusUnschedulable ScheduleTaskStatus = "UNSCHEDULABLE"
	ScheduleTaskStatusPartial       ScheduleTaskStatus = "PARTIAL"
)
