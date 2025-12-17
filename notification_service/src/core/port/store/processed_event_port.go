/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"
	"time"
)

// IProcessedEventPort defines the interface for processed event storage operations
type IProcessedEventPort interface {
	// IsEventProcessed checks if an event with the given ID has already been processed
	IsEventProcessed(ctx context.Context, eventID string) (bool, error)

	// MarkEventProcessed records that an event has been successfully processed
	MarkEventProcessed(ctx context.Context, eventID, eventType, topic string) error

	// CleanupOldEvents removes processed events older than the specified duration
	CleanupOldEvents(ctx context.Context, olderThan time.Duration) (int64, error)
}
