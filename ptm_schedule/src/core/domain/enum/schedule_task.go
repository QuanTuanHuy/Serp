package enum

type ScheduleTaskStatus string

const (
	ScheduleTaskPending       ScheduleTaskStatus = "PENDING"
	ScheduleTaskScheduled     ScheduleTaskStatus = "SCHEDULED"
	ScheduleTaskUnschedulable ScheduleTaskStatus = "UNSCHEDULABLE"
	ScheduleTaskPartial       ScheduleTaskStatus = "PARTIAL"
	ScheduleTaskCompleted     ScheduleTaskStatus = "COMPLETED"
	ScheduleTaskExcluded      ScheduleTaskStatus = "EXCLUDED"
)
