/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package kafkahandler

import (
	"context"
	"encoding/json"

	"github.com/serp/notification-service/src/core/domain/constant"
	"github.com/serp/notification-service/src/core/domain/dto/message"
	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/usecase"
	"github.com/serp/notification-service/src/kernel/utils"
	"go.uber.org/zap"
)

// UserNotificationHandler handles Kafka messages for user notification events.
type UserNotificationHandler struct {
	notificationUseCase usecase.INotificationUseCase
	middleware          *MessageProcessingMiddleware
	logger              *zap.Logger
}

func (h *UserNotificationHandler) GetWrappedHandler() MessageHandler {
	return h.middleware.WrapHandler(h.handleUserNotificationEvent)
}

func (h *UserNotificationHandler) handleUserNotificationEvent(
	ctx context.Context,
	topic, key string,
	value []byte,
	meta *message.MessageMetadata,
) error {
	h.logger.Info("Processing user notification event",
		zap.String("topic", topic),
		zap.String("key", key),
		zap.String("event_id", meta.EventID),
		zap.String("event_type", meta.EventType),
		zap.String("source", meta.Source),
	)

	var baseMessage message.BaseKafkaMessage
	if err := json.Unmarshal(value, &baseMessage); err != nil {
		h.logger.Error("Failed to unmarshal Kafka message", zap.Error(err))
		return err
	}

	switch meta.EventType {
	case constant.EventNotificationCreateRequested:
		var req request.CreateNotificationRequest
		if err := utils.BindKafkaMessageData(&baseMessage, &req); err != nil {
			h.logger.Error("Failed to bind create notification data", zap.Error(err))
			return err
		}
		_, err := h.notificationUseCase.CreateNotification(ctx, req.UserID, &req)
		return err

	case constant.EventNotificationBulkCreateRequested:
		var req request.CreateBulkNotificationRequest
		if err := utils.BindKafkaMessageData(&baseMessage, &req); err != nil {
			h.logger.Error("Failed to bind bulk create notification data", zap.Error(err))
			return err
		}
		return h.notificationUseCase.CreateBulkNotifications(ctx, &req)

	default:
		h.logger.Warn("Unhandled notification event type", zap.String("event_type", meta.EventType))
		return nil
	}
}

func NewUserNotificationHandler(
	notificationUseCase usecase.INotificationUseCase,
	middleware *MessageProcessingMiddleware,
	logger *zap.Logger,
) *UserNotificationHandler {
	if logger == nil {
		logger = zap.NewNop()
	}
	return &UserNotificationHandler{
		notificationUseCase: notificationUseCase,
		middleware:          middleware,
		logger:              logger,
	}
}
