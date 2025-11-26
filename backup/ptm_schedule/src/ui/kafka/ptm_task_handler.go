/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package kafkahandler

import (
	"context"
	"encoding/json"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/domain/mapper"
	"github.com/serp/ptm-schedule/src/core/usecase"
)

type PtmTaskHandler struct {
	scheduleTaskUseCase usecase.IScheduleTaskUseCase
}

func (c *PtmTaskHandler) HandleMessage(ctx context.Context, topic, key string, value []byte) error {
	log.Info(ctx, "Handling message for topic: ", topic, " with key: ", key, " value: ", string(value))

	var kafkaMessage message.KafkaMessage
	if err := json.Unmarshal(value, &kafkaMessage); err != nil {
		log.Error(ctx, "Failed to unmarshal Kafka message: ", err)
		return err
	}

	switch kafkaMessage.Cmd {
	case message.CREATE_TASK:
		var createTaskMsg message.KafkaCreateTaskMessage
		if err := message.BindData(&kafkaMessage, &createTaskMsg); err != nil {
			log.Error(ctx, "Failed to bind create task message data: ", err)
			return err
		}
		scheduleTask := mapper.ToScheduleTaskEntity(&createTaskMsg)
		_, err := c.scheduleTaskUseCase.CreateScheduleTask(ctx, createTaskMsg.UserID, scheduleTask)
		if err != nil {
			log.Error(ctx, "Failed to create schedule task: ", err)
			return err
		}
	case message.UPDATE_TASK:
		var updateTaskMsg message.KafkaUpdateTaskMessage
		if err := message.BindData(&kafkaMessage, &updateTaskMsg); err != nil {
			log.Error(ctx, "Failed to bind update task message data: ", err)
			return err
		}
		_, err := c.scheduleTaskUseCase.UpdateScheduleTask(ctx, updateTaskMsg.UserID, updateTaskMsg.TaskID, &updateTaskMsg)
		if err != nil {
			log.Error(ctx, "Failed to update schedule task: ", err)
			return err
		}
	case message.DELETE_TASK:
		var deleteTaskMsg message.KafkaDeleteTaskMessage
		if err := message.BindData(&kafkaMessage, &deleteTaskMsg); err != nil {
			log.Error(ctx, "Failed to bind delete task message data: ", err)
			return err
		}
		err := c.scheduleTaskUseCase.DeleteScheduleTask(ctx, deleteTaskMsg.TaskID)
		if err != nil {
			log.Error(ctx, "Failed to delete schedule task: ", err)
			return err
		}
	default:
		log.Warn(ctx, "Unknown Kafka command: ", kafkaMessage.Cmd)
	}
	return nil
}

func NewPtmTaskHandler(scheduleTaskUseCase usecase.IScheduleTaskUseCase) *PtmTaskHandler {
	return &PtmTaskHandler{
		scheduleTaskUseCase: scheduleTaskUseCase,
	}
}
