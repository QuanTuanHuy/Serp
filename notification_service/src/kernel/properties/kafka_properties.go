/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

type KafkaProperties struct {
	Producer KafkaProducerProperties `mapstructure:"producer"`
	Consumer KafkaConsumerProperties `mapstructure:"consumer"`
}

type KafkaProducerProperties struct {
	BootstrapServers []string `mapstructure:"bootstrapServers"`
	RequireAcks      int16    `mapstructure:"requireAcks"`
	MaxMessageBytes  int      `mapstructure:"maxMessageBytes"`
	RetryMax         int      `mapstructure:"retryMax"`
	RetryBackoffMs   int      `mapstructure:"retryBackoffMs"`
	FlushFrequencyMs int      `mapstructure:"flushFrequencyMs"`
	FlushMessages    int      `mapstructure:"flushMessages"`
}

type KafkaConsumerProperties struct {
	BootstrapServers []string `mapstructure:"bootstrapServers"`
	GroupID          string   `mapstructure:"groupId"`
	AutoOffsetReset  string   `mapstructure:"autoOffsetReset"`
	EnableAutoCommit bool     `mapstructure:"enableAutoCommit"`
	SessionTimeoutMs int      `mapstructure:"sessionTimeoutMs"`
	HeartBeatMs      int      `mapstructure:"heartBeatMs"`
	MaxPollRecords   int      `mapstructure:"maxPollRecords"`
	FetchMinBytes    int      `mapstructure:"fetchMinBytes"`
	FetchMaxWaitMs   int      `mapstructure:"fetchMaxWaitMs"`
}
