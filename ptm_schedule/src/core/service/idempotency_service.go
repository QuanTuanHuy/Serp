/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"time"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/core/domain/entity"
	port "github.com/serp/ptm-schedule/src/core/port/store"
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
	eventTTL           time.Duration
}

func (s *IdempotencyService) IsEventProcessed(ctx context.Context, eventID string) (bool, error) {
	processed, err := s.processedEventPort.IsEventProcessed(ctx, eventID)
	if err != nil {
		log.Error(ctx, "Failed to check if event is processed: ", err)
		return false, err
	}
	if processed {
		log.Info(ctx, "Event already processed, eventID: ", eventID)
	}
	return processed, nil
}

func (s *IdempotencyService) MarkEventProcessed(ctx context.Context, eventID, eventType, topic string) error {
	err := s.processedEventPort.MarkEventProcessed(ctx, eventID, eventType, topic)
	if err != nil {
		log.Error(ctx, "Failed to mark event as processed: ", err)
		return err
	}
	log.Info(ctx, "Event marked as processed, eventID: ", eventID)

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
		log.Error(ctx, "Failed to record failed event: ", err)
		return nil, err
	}

	result := &RetryResult{
		RetryCount:      updated.RetryCount,
		ShouldSendToDLQ: updated.RetryCount >= MaxRetryAttempts,
	}

	log.Info(ctx, "Recorded failed event. EventID: ", eventID,
		", RetryCount: ", result.RetryCount,
		", ShouldSendToDLQ: ", result.ShouldSendToDLQ)

	return result, nil
}

func (s *IdempotencyService) MarkEventSentToDLQ(ctx context.Context, eventID string) error {
	err := s.failedEventPort.MarkAsSentToDLQ(ctx, eventID)
	if err != nil {
		log.Error(ctx, "Failed to mark event as sent to DLQ: ", err)
		return err
	}
	log.Info(ctx, "Event marked as sent to DLQ, eventID: ", eventID)
	return nil
}

func (s *IdempotencyService) ClearFailedEvent(ctx context.Context, eventID string) error {
	return s.failedEventPort.DeleteFailedEvent(ctx, eventID)
}

func (s *IdempotencyService) CleanupExpiredEvents(ctx context.Context) (int64, error) {
	count, err := s.processedEventPort.CleanupOldEvents(ctx, s.eventTTL)
	if err != nil {
		log.Error(ctx, "Failed to cleanup expired events: ", err)
		return 0, err
	}
	if count > 0 {
		log.Info(ctx, "Cleaned up expired events, count: ", count)
	}
	return count, nil
}

func NewIdempotencyService(
	processedEventPort port.IProcessedEventPort,
	failedEventPort port.IFailedEventPort,
) IIdempotencyService {
	return &IdempotencyService{
		processedEventPort: processedEventPort,
		failedEventPort:    failedEventPort,
		eventTTL:           DefaultEventTTL,
	}
}
