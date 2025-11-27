/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type KafkaTopic string

const (
	TASK_MANAGER_TOPIC  KafkaTopic = "serp.ptm.task.events"
	SCHEDULE_TASK_TOPIC KafkaTopic = "ptm.schedule-task.topic"
)
