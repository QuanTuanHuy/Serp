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
	"github.com/golibs-starter/golib/log"
	"github.com/serp/ptm-schedule/src/kernel/properties"
	kafkahandler "github.com/serp/ptm-schedule/src/ui/kafka"
)

type KafkaConsumer struct {
	consumerGroup      sarama.ConsumerGroup
	topics             []string
	consumerProperties *properties.KafkaConsumerProperties
	handlers           map[string]kafkahandler.MessageHandler
	handlersMutex      sync.RWMutex
	wg                 sync.WaitGroup
}

func NewKafkaConsumer(consumerProperties *properties.KafkaConsumerProperties) (*KafkaConsumer, error) {
	config := sarama.NewConfig()
	config.Version = sarama.V2_8_0_0
	config.Consumer.Group.Rebalance.Strategy = sarama.NewBalanceStrategyRoundRobin()
	config.Consumer.Offsets.Initial = sarama.OffsetOldest

	if consumerProperties.AutoOffsetReset == "latest" {
		config.Consumer.Offsets.Initial = sarama.OffsetNewest
	}
	config.Consumer.Group.Session.Timeout = time.Duration(consumerProperties.SessionTimeoutMs) * time.Millisecond
	config.Consumer.Group.Heartbeat.Interval = time.Duration(consumerProperties.HeartbeatMs) * time.Millisecond
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
	}, nil
}

func (k *KafkaConsumer) StartConsumer(ctx context.Context) error {
	k.wg.Go(func() {
		handler := &ConsumerGroupHandler{kafkaConsumer: k}
		for {
			select {
			case <-ctx.Done():
				log.Info(ctx, "Consumer context cancelled, stopping consumer")
				return
			default:
				if err := k.consumerGroup.Consume(ctx, k.topics, handler); err != nil {
					log.Error(ctx, "Error consuming messages: ", err)
					time.Sleep(time.Second)
					continue
				}
			}
		}
	})
	return nil
}

func (k *KafkaConsumer) Close() error {
	log.Info(context.Background(), "Closing Kafka consumer...")
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
				log.Warn(ctx, "No hanlder registered for topic: ", message.Topic)
				session.MarkMessage(message, "")
				continue
			}

			key := string(message.Key)
			err := handler(ctx, message.Topic, key, message.Value)
			if err != nil {
				log.Error(ctx, "Error processing message from topic ", message.Topic, " with key ", key, ": ", err)
			} else {
				log.Info(ctx, "Successfully processed message from topic ", message.Topic, " with key ", key)
			}

			session.MarkMessage(message, "")

		case <-session.Context().Done():
			return nil
		}
	}
}
