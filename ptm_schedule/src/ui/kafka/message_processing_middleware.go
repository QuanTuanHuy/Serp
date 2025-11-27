/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package kafkahandler

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
	port "github.com/serp/ptm-schedule/src/core/port/client"
	"github.com/serp/ptm-schedule/src/core/service"
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
}

// WrapHandler wraps a message handler with idempotency checking, retry tracking, and DLQ support
func (m *MessageProcessingMiddleware) WrapHandler(handler MessageHandlerFunc) MessageHandler {
	return func(ctx context.Context, topic, key string, value []byte) error {
		var baseMessage message.BaseKafkaMessage
		if err := json.Unmarshal(value, &baseMessage); err != nil {
			log.Error(ctx, "Failed to unmarshal Kafka message: ", err)
			m.sendToDLQ(ctx, topic, key, value, "unmarshal_error", err.Error(), "")
			return nil
		}

		eventID := baseMessage.GetEventID()
		eventType := baseMessage.GetEventType()

		processed, err := m.idempotencyService.IsEventProcessed(ctx, eventID)
		if err != nil {
			log.Error(ctx, "Failed to check idempotency, will retry: ", err)
			return err
		}
		if processed {
			log.Info(ctx, "Duplicate event detected, skipping. EventID: ", eventID, ", EventType: ", eventType)
			return nil
		}

		meta := baseMessage.GetMeta()
		handlerErr := handler(ctx, topic, key, value, &meta)
		if handlerErr != nil {
			log.Error(ctx, "Handler failed for event: ", eventID, ", error: ", handlerErr)

			retryResult, recordErr := m.idempotencyService.RecordFailedEvent(
				ctx, eventID, eventType, topic, key, string(value), handlerErr.Error(),
			)
			if recordErr != nil {
				log.Error(ctx, "Failed to record failed event, will retry via Kafka: ", recordErr)
				return handlerErr
			}

			if retryResult.ShouldSendToDLQ {
				log.Warn(ctx, "Max retries reached for event: ", eventID,
					", RetryCount: ", retryResult.RetryCount,
					", sending to DLQ")
				m.sendToDLQ(ctx, topic, key, value, "max_retries_exceeded", handlerErr.Error(), eventID)

				_ = m.idempotencyService.MarkEventSentToDLQ(ctx, eventID)

				return nil
			}

			log.Info(ctx, "Event will be retried. EventID: ", eventID,
				", RetryCount: ", retryResult.RetryCount,
				", MaxRetries: ", service.MaxRetryAttempts)
			return handlerErr
		}

		if err := m.idempotencyService.MarkEventProcessed(ctx, eventID, eventType, topic); err != nil {
			log.Error(ctx, "Failed to mark event as processed, but handler succeeded. EventID: ", eventID)
			// Worst case: event might be reprocessed, but handler should be idempotent
		}

		log.Info(ctx, "Successfully processed event. EventID: ", eventID, ", EventType: ", eventType)
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
		log.Error(ctx, "Failed to send message to DLQ: ", err,
			", originalTopic: ", originalTopic,
			", key: ", key,
			", errorType: ", errorType)
	} else {
		log.Warn(ctx, "Message sent to DLQ. Topic: ", dlqTopic,
			", Key: ", key,
			", EventID: ", eventID,
			", ErrorType: ", errorType,
			", ErrorMessage: ", errorMessage)
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
