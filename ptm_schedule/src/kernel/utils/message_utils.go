package utils

import (
	"context"
	"encoding/json"
	"time"

	"github.com/google/uuid"
	"github.com/serp/ptm-schedule/src/core/domain/dto/message"
)

const (
	DefaultVersion = "1.0"
	TraceIDKey     = "X-Trace-ID"
	Source         = "ptm-schedule"
)

func NewMessage(ctx context.Context, source string, eventType string, data any) message.BaseKafkaMessage {
	traceID, ok := ctx.Value(TraceIDKey).(string)
	if !ok || traceID == "" {
		traceID = uuid.New().String()
	}

	meta := message.MessageMetadata{
		EventID:   uuid.New().String(),
		EventType: eventType,
		Source:    source,
		Version:   DefaultVersion,
		Timestamp: time.Now().UnixMilli(),
		TraceID:   traceID,
	}

	return message.BaseKafkaMessage{
		Meta: meta,
		Data: data,
	}
}

func BuildCreatedEvent(ctx context.Context, entityName string, data any) message.BaseKafkaMessage {
	eventType := "ptm" + "." + entityName + ".created"
	return NewMessage(ctx, Source, eventType, data)
}

func BuildUpdatedEvent(ctx context.Context, entityName string, data any) message.BaseKafkaMessage {
	eventType := "ptm" + "." + entityName + ".updated"
	return NewMessage(ctx, Source, eventType, data)
}

func BuildDeletedEvent(ctx context.Context, entityName string, data any) message.BaseKafkaMessage {
	eventType := "ptm" + "." + entityName + ".deleted"
	return NewMessage(ctx, Source, eventType, data)
}

func BindData(k *message.BaseKafkaMessage, dst any) error {
	dataBytes, err := json.Marshal(k.GetData())
	if err != nil {
		return err
	}
	if err := json.Unmarshal(dataBytes, dst); err != nil {
		return err
	}
	return nil
}
