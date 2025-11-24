/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package properties

import "github.com/golibs-starter/golib/config"

type KafkaProducerProperties struct {
	BootstrapServers []string
	RequireAcks      int16
	MaxMessageBytes  int
	RetryMax         int
	RetryBackoffMs   int
	FlushFrequencyMs int
	FlushMessages    int
}

func (k KafkaProducerProperties) Prefix() string {
	return "app.kafka.producer"
}

func NewKafkaProducerProperties(loader config.Loader) (*KafkaProducerProperties, error) {
	props := KafkaProducerProperties{}
	err := loader.Bind(&props)
	return &props, err
}
