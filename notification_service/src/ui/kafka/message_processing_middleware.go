/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package kafkahandler

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/serp/notification-service/src/core/domain/dto/message"
	port "github.com/serp/notification-service/src/core/port/client"
	"github.com/serp/notification-service/src/core/service"
	"go.uber.org/zap"
)

const (
	DLQTopicSuffix = ".dlq"
)

// MessageHandler is a function type that processes Kafka messages
type MessageHandler func(ctx context.Context, topic, key string, value []byte) error

// MessageHandlerFunc is the actual business logic handler
type MessageHandlerFunc func(ctx context.Context, topic, key string, value []byte, meta *message.MessageMetadata) error

// MessageProcessingMiddleware wraps message handlers with idempotency, retry, and DLQ logic
type MessageProcessingMiddleware struct {
	idempotencyService service.IIdempotencyService
	kafkaProducer      port.IKafkaProducerPort

	logger *zap.Logger
}

// WrapHandler wraps a message handler with idempotency checking, retry tracking, and DLQ support
func (m *MessageProcessingMiddleware) WrapHandler(handler MessageHandlerFunc) MessageHandler {
	return func(ctx context.Context, topic, key string, value []byte) error {
		var baseMessage message.BaseKafkaMessage
		if err := json.Unmarshal(value, &baseMessage); err != nil {
			m.logger.Error("Failed to unmarshal Kafka message", zap.Error(err))
			m.sendToDLQ(ctx, topic, key, value, "unmarshal_error", err.Error(), "")
			return nil
		}

		eventID := baseMessage.GetEventID()
		eventType := baseMessage.GetEventType()

		processed, err := m.idempotencyService.IsEventProcessed(ctx, eventID)
		if err != nil {
			m.logger.Error("Failed to check idempotency, will retry", zap.String("event_id", eventID), zap.Error(err))
			return err
		}
		if processed {
			m.logger.Info("Duplicate event detected, skipping", zap.String("event_id", eventID), zap.String("event_type", eventType))
			return nil
		}

		meta := baseMessage.GetMeta()
		handlerErr := handler(ctx, topic, key, value, &meta)
		if handlerErr != nil {
			m.logger.Error("Handler failed for event", zap.String("event_id", eventID), zap.Error(handlerErr))

			retryResult, recordErr := m.idempotencyService.RecordFailedEvent(
				ctx, eventID, eventType, topic, key, string(value), handlerErr.Error(),
			)
			if recordErr != nil {
				m.logger.Error("Failed to record failed event, will retry via Kafka", zap.Error(recordErr))
				return handlerErr
			}

			if retryResult.ShouldSendToDLQ {
				m.logger.Warn("Max retries reached for event", zap.String("event_id", eventID))
				m.sendToDLQ(ctx, topic, key, value, "max_retries_exceeded", handlerErr.Error(), eventID)

				_ = m.idempotencyService.MarkEventSentToDLQ(ctx, eventID)

				return nil
			}

			m.logger.Info("Event will be retried", zap.String("event_id", eventID),
				zap.Int("retry_count", retryResult.RetryCount),
				zap.Int("max_retries", service.MaxRetryAttempts))
			return handlerErr
		}

		if err := m.idempotencyService.MarkEventProcessed(ctx, eventID, eventType, topic); err != nil {
			m.logger.Error("Failed to mark event as processed, but handler succeeded", zap.String("event_id", eventID))
			// Worst case: event might be reprocessed, but handler should be idempotent
		}

		m.logger.Info("Successfully processed event", zap.String("event_id", eventID), zap.String("event_type", eventType))
		return nil
	}
}

func (m *MessageProcessingMiddleware) sendToDLQ(ctx context.Context, originalTopic, key string, value []byte, errorType, errorMessage, eventID string) {
	dlqTopic := originalTopic + DLQTopicSuffix

	dlqMessage := map[string]any{
		"originalTopic": originalTopic,
		"originalKey":   key,
		"originalValue": string(value),
		"errorType":     errorType,
		"errorMessage":  errorMessage,
		"eventId":       eventID,
	}

	err := m.kafkaProducer.SendMessageAsync(ctx, dlqTopic, key, dlqMessage)
	if err != nil {
		m.logger.Error("Failed to send message to DLQ",
			zap.String("original_topic", originalTopic),
			zap.String("key", key),
			zap.String("error_type", errorType),
			zap.Error(err))
	} else {
		m.logger.Warn("Message sent to DLQ",
			zap.String("topic", dlqTopic),
			zap.String("key", key),
			zap.String("event_id", eventID),
			zap.String("error_type", errorType),
			zap.String("error_message", errorMessage))
	}
}

func NewMessageProcessingMiddleware(
	idempotencyService service.IIdempotencyService,
	kafkaProducer port.IKafkaProducerPort,
) *MessageProcessingMiddleware {
	return &MessageProcessingMiddleware{
		idempotencyService: idempotencyService,
		kafkaProducer:      kafkaProducer,
	}
}

type ProcessingResult struct {
	EventID   string
	EventType string
	Success   bool
	Error     error
	SentToDLQ bool
}

func (r ProcessingResult) String() string {
	if r.Success {
		return fmt.Sprintf("EventID=%s, EventType=%s, Success=true", r.EventID, r.EventType)
	}
	return fmt.Sprintf("EventID=%s, EventType=%s, Success=false, Error=%v, SentToDLQ=%v",
		r.EventID, r.EventType, r.Error, r.SentToDLQ)
}
