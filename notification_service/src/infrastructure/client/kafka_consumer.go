/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"sync"
	"time"

	"github.com/IBM/sarama"
	"github.com/serp/notification-service/src/kernel/properties"
	kafkahandler "github.com/serp/notification-service/src/ui/kafka"
	"go.uber.org/zap"
)

type KafkaConsumer struct {
	consumerGroup      sarama.ConsumerGroup
	topics             []string
	consumerProperties properties.KafkaConsumerProperties
	handlers           map[string]kafkahandler.MessageHandler
	handlersMutex      sync.RWMutex
	wg                 sync.WaitGroup
	logger             *zap.Logger
}

func NewKafkaConsumer(appProps *properties.AppProperties, logger *zap.Logger) (*KafkaConsumer, error) {
	consumerProperties := appProps.Kafka.Consumer

	config := sarama.NewConfig()
	config.Version = sarama.V2_8_0_0
	config.Consumer.Group.Rebalance.Strategy = sarama.NewBalanceStrategyRoundRobin()
	config.Consumer.Offsets.Initial = sarama.OffsetOldest

	if consumerProperties.AutoOffsetReset == "latest" {
		config.Consumer.Offsets.Initial = sarama.OffsetNewest
	}
	config.Consumer.Group.Session.Timeout = time.Duration(consumerProperties.SessionTimeoutMs) * time.Millisecond
	config.Consumer.Group.Heartbeat.Interval = time.Duration(consumerProperties.HeartBeatMs) * time.Millisecond
	config.Consumer.Return.Errors = true
	config.Consumer.Fetch.Min = int32(consumerProperties.FetchMinBytes)
	config.Consumer.MaxWaitTime = time.Duration(consumerProperties.FetchMaxWaitMs) * time.Millisecond

	consumerGroup, err := sarama.NewConsumerGroup(consumerProperties.BootstrapServers, consumerProperties.GroupID, config)
	if err != nil {
		return nil, fmt.Errorf("failed to create consumer group: %w", err)
	}

	return &KafkaConsumer{
		consumerGroup:      consumerGroup,
		topics:             []string{},
		consumerProperties: consumerProperties,
		handlers:           make(map[string]kafkahandler.MessageHandler),
		logger:             logger,
	}, nil
}

func (k *KafkaConsumer) StartConsumer(ctx context.Context) error {
	k.wg.Go(func() {
		handler := &ConsumerGroupHandler{kafkaConsumer: k}
		for {
			select {
			case <-ctx.Done():
				k.logger.Info("Consumer context cancelled, stopping consumer")
				return
			default:
				if err := k.consumerGroup.Consume(ctx, k.topics, handler); err != nil {
					k.logger.Error("Error consuming messages: ", zap.Error(err))
					time.Sleep(time.Second)
					continue
				}
			}
		}
	})
	return nil
}

func (k *KafkaConsumer) Close() error {
	k.logger.Info("Closing Kafka consumer...")
	k.wg.Wait()
	if k.consumerGroup != nil {
		return k.consumerGroup.Close()
	}
	return nil
}

func (k *KafkaConsumer) RegisterHandler(topic string, handler kafkahandler.MessageHandler) {
	k.handlersMutex.Lock()
	defer k.handlersMutex.Unlock()
	k.handlers[topic] = handler
}

func (k *KafkaConsumer) Subscribe(topics []string) error {
	k.topics = topics
	return nil
}

type ConsumerGroupHandler struct {
	kafkaConsumer *KafkaConsumer
}

func (h *ConsumerGroupHandler) Setup(sarama.ConsumerGroupSession) error {
	return nil
}

func (h *ConsumerGroupHandler) Cleanup(sarama.ConsumerGroupSession) error {
	return nil
}

func (h *ConsumerGroupHandler) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for {
		select {
		case message := <-claim.Messages():
			if message == nil {
				return nil
			}

			ctx := context.Background()
			h.kafkaConsumer.handlersMutex.RLock()
			handler, exists := h.kafkaConsumer.handlers[message.Topic]
			h.kafkaConsumer.handlersMutex.RUnlock()

			if !exists {
				h.kafkaConsumer.logger.Warn("No handler registered for topic: ", zap.String("topic", message.Topic))
				session.MarkMessage(message, "")
				continue
			}

			key := string(message.Key)
			err := handler(ctx, message.Topic, key, message.Value)
			if err != nil {
				h.kafkaConsumer.logger.Error("Error processing message from topic ", zap.String("topic", message.Topic), zap.String("key", key), zap.Error(err))
			} else {
				session.MarkMessage(message, "")
				h.kafkaConsumer.logger.Info("Successfully processed message from topic ", zap.String("topic", message.Topic), zap.String("key", key))
			}

		case <-session.Context().Done():
			return nil
		}
	}
}
