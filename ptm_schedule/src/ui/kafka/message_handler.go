package kafkahandler

import "context"

type MessageHandler func(ctx context.Context, topic string, key string, value []byte) error
