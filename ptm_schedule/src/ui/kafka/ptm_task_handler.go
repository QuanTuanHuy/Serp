/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package kafkahandler

import (
	"context"
	"encoding/json"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/constant"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	"github.com/serp/ptm-schedule/src/core/usecase"
	"github.com/serp/ptm-schedule/src/kernel/utils"
)

// PtmTaskHandler handles Kafka messages from ptm_task service
type PtmTaskHandler struct {
	scheduleTaskUseCase usecase.IScheduleTaskUseCase
	middleware          *MessageProcessingMiddleware
}

func (c *PtmTaskHandler) GetWrappedHandler() MessageHandler {
	return c.middleware.WrapHandler(c.handleTaskEvent)
}

func (c *PtmTaskHandler) handleTaskEvent(ctx context.Context, topic, key string, value []byte, meta *message.MessageMetadata) error {
	log.Info(ctx, "Processing task event. Topic: ", topic, ", Key: ", key,
		", EventID: ", meta.EventID, ", EventType: ", meta.EventType)

	var kafkaMessage message.BaseKafkaMessage
	if err := json.Unmarshal(value, &kafkaMessage); err != nil {
		log.Error(ctx, "Failed to unmarshal Kafka message: ", err)
		return err
	}

	switch meta.EventType {
	case constant.TaskCreatedEvent:
		var createdEvent message.TaskCreatedEvent
		if err := utils.BindData(&kafkaMessage, &createdEvent); err != nil {
			log.Error(ctx, "Failed to bind create task message data: ", err)
			return err
		}
		err := c.scheduleTaskUseCase.HandleTaskCreated(ctx, &createdEvent)
		if err != nil {
			log.Error(ctx, "Failed to sync schedule task from source: ", err)
			return err
		}
		log.Info(ctx, "Successfully synced task from source. TaskID: ", createdEvent.TaskID)

	case constant.TaskUpdatedEvent:
		var updatedEvent message.TaskUpdatedEvent
		if err := utils.BindData(&kafkaMessage, &updatedEvent); err != nil {
			log.Error(ctx, "Failed to bind update task message data: ", err)
			return err
		}
		err := c.scheduleTaskUseCase.HandleTaskUpdated(ctx, &updatedEvent)
		if err != nil {
			log.Error(ctx, "Failed to update schedule task from source: ", err)
			return err
		}
		log.Info(ctx, "Successfully updated task from source. TaskID: ", updatedEvent.TaskID)

	case constant.TaskBulkDeletedEvent:
		var bulkDeletedEvent message.TaskBulkDeletedEvent
		if err := utils.BindData(&kafkaMessage, &bulkDeletedEvent); err != nil {
			log.Error(ctx, "Failed to bind bulk delete task message data: ", err)
			return err
		}
		err := c.scheduleTaskUseCase.HandleTaskBulkDeleted(ctx, &bulkDeletedEvent)
		if err != nil {
			log.Error(ctx, "Failed to bulk delete schedule tasks from source: ", err)
			return err
		}

	case constant.TaskDeletedEvent:
		var deletedEvent message.TaskDeletedEvent
		if err := utils.BindData(&kafkaMessage, &deletedEvent); err != nil {
			log.Error(ctx, "Failed to bind delete task message data: ", err)
			return err
		}
		err := c.scheduleTaskUseCase.HandleTaskDeleted(ctx, &deletedEvent)
		if err != nil {
			log.Error(ctx, "Failed to delete schedule task from source: ", err)
			return err
		}
		log.Info(ctx, "Successfully deleted task from source. TaskID: ", deletedEvent.TaskID)

	default:
		log.Warn(ctx, "Unhandled event type: ", meta.EventType)
	}

	return nil
}

func NewPtmTaskHandler(
	scheduleTaskUseCase usecase.IScheduleTaskUseCase,
	middleware *MessageProcessingMiddleware,
) *PtmTaskHandler {
	return &PtmTaskHandler{
		scheduleTaskUseCase: scheduleTaskUseCase,
		middleware:          middleware,
	}
}
