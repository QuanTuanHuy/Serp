/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"github.com/IBM/sarama"
	port "github.com/serp/pm-core/src/core/port/client"
	"github.com/serp/pm-core/src/kernel/properties"
	"go.uber.org/zap"
)

type KafkaProducerAdapter struct {
	syncProducer       sarama.SyncProducer
	asyncProducer      sarama.AsyncProducer
	producerProperties *properties.KafkaProducerProperties
	logger             *zap.Logger
}

func (k *KafkaProducerAdapter) SendMessage(ctx context.Context, topic string, key string, payload any) error {
	message, err := k.ToSaramaMessage(topic, key, payload)
	if err != nil {
		k.logger.Error(fmt.Sprintf("Failed to create message for topic %s with key %s", topic, key), zap.Error(err))
		return err
	}
	partition, offset, err := k.syncProducer.SendMessage(message)
	if err != nil {
		k.logger.Error(fmt.Sprintf("Failed to send message to topic %s with key %s", topic, key), zap.Error(err))
		return err
	}
	k.logger.Info(fmt.Sprintf("Message sent successfully to topic %s with key %s at partition %d and offset %d", topic, key, partition, offset))
	return nil
}

func (k *KafkaProducerAdapter) SendMessageAsync(ctx context.Context, topic string, key string, payload any) error {
	message, err := k.ToSaramaMessage(topic, key, payload)
	if err != nil {
		k.logger.Error(fmt.Sprintf("Failed to create message for topic %s with key %s", topic, key), zap.Error(err))
		return err
	}

	select {
	case k.asyncProducer.Input() <- message:
		k.logger.Info(fmt.Sprintf("Async message sent to topic %s with key %s", topic, key))
		return nil
	default:
		k.logger.Error(fmt.Sprintf("Failed to send async message to topic %s with key %s due to full channel", topic, key))
		return sarama.ErrOutOfBrokers
	}
}

func (k *KafkaProducerAdapter) ToSaramaMessage(topic string, key string, payload any) (*sarama.ProducerMessage, error) {
	payloadBytes, err := json.Marshal(payload)
	if err != nil {
		k.logger.Error(fmt.Sprintf("Failed to marshal payload for topic %s with key %s", topic, key), zap.Error(err))
		return nil, err
	}
	k.logger.Info(fmt.Sprintf("Creating message for topic %s with key %s and payload %s", topic, key, string(payloadBytes)))

	return &sarama.ProducerMessage{
		Topic: topic,
		Key:   sarama.StringEncoder(key),
		Value: sarama.ByteEncoder(payloadBytes),
	}, nil
}

func (k *KafkaProducerAdapter) HandleAsyncProducerMessages() {
	for {
		select {
		case success := <-k.asyncProducer.Successes():
			k.logger.Info(fmt.Sprintf("Message sent successfully to topic %s with key %s", success.Topic, success.Key))
		case err := <-k.asyncProducer.Errors():
			k.logger.Error(fmt.Sprintf("Failed to send message to topic %s with key %s error: %v", err.Msg.Topic, err.Msg.Key, err.Err))
		}
	}
}

func NewKafkaProducerAdapter(appProperties *properties.AppProperties, logger *zap.Logger) port.IKafkaProducerPort {
	producerProperties := &appProperties.Kafka.Producer

	saramaConfig := sarama.NewConfig()

	saramaConfig.Producer.Return.Successes = true
	saramaConfig.Producer.RequiredAcks = sarama.RequiredAcks(producerProperties.RequireAcks)
	saramaConfig.Producer.Retry.Max = producerProperties.RetryMax
	saramaConfig.Producer.Retry.Backoff = time.Duration(producerProperties.RetryBackoffMs) * time.Millisecond

	if producerProperties.MaxMessageBytes > 0 {
		saramaConfig.Producer.MaxMessageBytes = producerProperties.MaxMessageBytes
	}
	saramaConfig.Producer.Flush.Frequency = time.Duration(producerProperties.FlushFrequencyMs) * time.Millisecond
	saramaConfig.Producer.Flush.MaxMessages = producerProperties.FlushMessages

	syncProducer, err := sarama.NewSyncProducer(producerProperties.BootstrapServers, saramaConfig)
	if err != nil {
		panic("Failed to create sync producer: " + err.Error())
	}

	asyncProducer, err := sarama.NewAsyncProducer(producerProperties.BootstrapServers, saramaConfig)
	if err != nil {
		panic("Failed to create async producer: " + err.Error())
	}

	kafkaProducerAdapter := &KafkaProducerAdapter{
		syncProducer:       syncProducer,
		asyncProducer:      asyncProducer,
		producerProperties: producerProperties,
		logger:             logger,
	}
	go kafkaProducerAdapter.HandleAsyncProducerMessages()

	return kafkaProducerAdapter
}
