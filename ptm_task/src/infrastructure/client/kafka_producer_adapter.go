package adapter

import (
	"context"
	"encoding/json"
	"time"

	"github.com/IBM/sarama"
	"github.com/golibs-starter/golib/log"
	port "github.com/serp/ptm-task/src/core/port/client"
	"github.com/serp/ptm-task/src/kernel/properties"
)

type KafkaProducerAdapter struct {
	syncProducer       sarama.SyncProducer
	asyncProducer      sarama.AsyncProducer
	producerProperties *properties.KafkaProducerProperties
}

func (k *KafkaProducerAdapter) SendMessage(ctx context.Context, topic string, key string, payload any) error {
	message, err := k.ToSaramaMessage(topic, key, payload)
	if err != nil {
		log.Error(ctx, "Failed to create message for topic ", topic, " with key ", key, " error: ", err)
		return err
	}
	partition, offset, err := k.syncProducer.SendMessage(message)
	if err != nil {
		log.Error(ctx, "Failed to send message to topic ", topic, " with key ", key, " error: ", err)
		return err
	}
	log.Info(ctx, "Message sent successfully to topic ", topic, " with key ", key, " at partition ", partition, " and offset ", offset)
	return nil
}

func (k *KafkaProducerAdapter) SendMessageAsync(ctx context.Context, topic string, key string, payload any) error {
	message, err := k.ToSaramaMessage(topic, key, payload)
	if err != nil {
		log.Error(ctx, "Failed to create message for topic ", topic, " with key ", key, " error: ", err)
		return err
	}

	select {
	case k.asyncProducer.Input() <- message:
		log.Info(ctx, "Async message sent to topic ", topic, " with key ", key)
		return nil
	default:
		log.Error(ctx, "Failed to send async message to topic ", topic, " with key ", key, " due to full channel")
		return sarama.ErrOutOfBrokers
	}
}

func (k *KafkaProducerAdapter) ToSaramaMessage(topic string, key string, payload any) (*sarama.ProducerMessage, error) {
	payloadBytes, err := json.Marshal(payload)
	if err != nil {
		log.Error("Failed to marshal payload: ", err)
		return nil, err
	}
	log.Info("Creating message for topic ", topic, " with key ", key, " and payload ", string(payloadBytes))

	return &sarama.ProducerMessage{
		Topic: topic,
		Key:   sarama.StringEncoder(key),
		Value: sarama.ByteEncoder(payloadBytes),
	}, nil
}

func (k *KafkaProducerAdapter) HandleAsyncProducerMessages() {
	ctx := context.Background()

	for {
		select {
		case success := <-k.asyncProducer.Successes():
			log.Info(ctx, "Message sent successfully to topic ", success.Topic, " with key ", success.Key)
		case err := <-k.asyncProducer.Errors():
			log.Error(ctx, "Failed to send message to topic ", err.Msg.Topic, " with key ", err.Msg.Key, " error: ", err.Err)
		}
	}
}

func NewKafkaProducerAdapter(producerProperties *properties.KafkaProducerProperties) port.IKafkaProducerPort {
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
	}
	go kafkaProducerAdapter.HandleAsyncProducerMessages()

	return kafkaProducerAdapter
}
