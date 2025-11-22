/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

type KafkaProperties struct {
	Producer KafkaProducerProperties `mapstructure:"producer"`
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
