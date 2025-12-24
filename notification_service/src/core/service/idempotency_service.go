/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"time"

	"github.com/serp/notification-service/src/core/domain/entity"
	port "github.com/serp/notification-service/src/core/port/store"
	"go.uber.org/zap"
)

const (
	DefaultEventTTL = 24 * time.Hour

	MaxRetryAttempts = 3
)

type RetryResult struct {
	RetryCount      int
	ShouldSendToDLQ bool
}

// IIdempotencyService provides idempotency checking for Kafka message processing
type IIdempotencyService interface {
	IsEventProcessed(ctx context.Context, eventID string) (bool, error)

	// MarkEventProcessed marks an event as successfully processed
	MarkEventProcessed(ctx context.Context, eventID, eventType, topic string) error

	// RecordFailedEvent records a failed event and returns retry information
	// Returns RetryResult with current retry count and whether to send to DLQ
	RecordFailedEvent(ctx context.Context, eventID, eventType, topic, key, value, errorMsg string) (*RetryResult, error)

	// MarkEventSentToDLQ marks a failed event as sent to DLQ
	MarkEventSentToDLQ(ctx context.Context, eventID string) error

	// ClearFailedEvent removes failed event record after successful retry
	ClearFailedEvent(ctx context.Context, eventID string) error

	// CleanupExpiredEvents removes events older than TTL
	CleanupExpiredEvents(ctx context.Context) (int64, error)
}

type IdempotencyService struct {
	processedEventPort port.IProcessedEventPort
	failedEventPort    port.IFailedEventPort

	logger *zap.Logger

	eventTTL time.Duration
}

func (s *IdempotencyService) IsEventProcessed(ctx context.Context, eventID string) (bool, error) {
	processed, err := s.processedEventPort.IsEventProcessed(ctx, eventID)
	if err != nil {
		s.logger.Error("Failed to check if event is processed: ", zap.Error(err))
		return false, err
	}
	if processed {
		s.logger.Info("Event already processed, eventID: ", zap.String("event_id", eventID))
	}
	return processed, nil
}

func (s *IdempotencyService) MarkEventProcessed(ctx context.Context, eventID, eventType, topic string) error {
	err := s.processedEventPort.MarkEventProcessed(ctx, eventID, eventType, topic)
	if err != nil {
		s.logger.Error("Failed to mark event as processed: ", zap.Error(err))
		return err
	}
	s.logger.Info("Event marked as processed, eventID: ", zap.String("event_id", eventID))

	_ = s.failedEventPort.DeleteFailedEvent(ctx, eventID)

	return nil
}

func (s *IdempotencyService) RecordFailedEvent(ctx context.Context, eventID, eventType, topic, key, value, errorMsg string) (*RetryResult, error) {
	failedEvent := &entity.FailedEventEntity{
		EventID:      eventID,
		EventType:    eventType,
		Topic:        topic,
		MessageKey:   key,
		MessageValue: value,
		LastError:    errorMsg,
	}

	updated, err := s.failedEventPort.RecordFailedEvent(ctx, failedEvent)
	if err != nil {
		s.logger.Error("Failed to record failed event: ", zap.Error(err))
		return nil, err
	}

	result := &RetryResult{
		RetryCount:      updated.RetryCount,
		ShouldSendToDLQ: updated.RetryCount >= MaxRetryAttempts,
	}

	s.logger.Info("Recorded failed event. EventID: ", zap.String("event_id", eventID),
		zap.Int("retry_count", result.RetryCount),
		zap.Bool("should_send_to_dlq", result.ShouldSendToDLQ))

	return result, nil
}

func (s *IdempotencyService) MarkEventSentToDLQ(ctx context.Context, eventID string) error {
	err := s.failedEventPort.MarkAsSentToDLQ(ctx, eventID)
	if err != nil {
		s.logger.Error("Failed to mark event as sent to DLQ: ", zap.Error(err))
		return err
	}
	s.logger.Info("Event marked as sent to DLQ, eventID: ", zap.String("event_id", eventID))
	return nil
}

func (s *IdempotencyService) ClearFailedEvent(ctx context.Context, eventID string) error {
	return s.failedEventPort.DeleteFailedEvent(ctx, eventID)
}

func (s *IdempotencyService) CleanupExpiredEvents(ctx context.Context) (int64, error) {
	count, err := s.processedEventPort.CleanupOldEvents(ctx, s.eventTTL)
	if err != nil {
		s.logger.Error("Failed to cleanup expired events: ", zap.Error(err))
		return 0, err
	}
	if count > 0 {
		s.logger.Info("Cleaned up expired events, count: ", zap.Int64("count", count))
	}
	return count, nil
}

func NewIdempotencyService(
	processedEventPort port.IProcessedEventPort,
	failedEventPort port.IFailedEventPort,
	logger *zap.Logger,
) IIdempotencyService {
	return &IdempotencyService{
		processedEventPort: processedEventPort,
		failedEventPort:    failedEventPort,
		logger:             logger,
		eventTTL:           DefaultEventTTL,
	}
}
