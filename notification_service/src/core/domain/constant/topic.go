package constant

type KafkaTopic string

const (
	TASK_TOPIC          KafkaTopic = "serp.ptm.task.events"
	SCHEDULE_TASK_TOPIC KafkaTopic = "serp.ptm.scheduled_tasks.events"
)
