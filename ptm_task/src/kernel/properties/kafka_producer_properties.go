package properties

import "github.com/golibs-starter/golib/config"

type KafkaProducerProperties struct {
	BootstrapServers []string `default:"localhost:9094" mapstructure:"bootstrapServers"`
	RequireAcks      int16    `default:"-1" mapstructure:"requireAcks"`
	MaxMessageBytes  int      `default:"0" mapstructure:"maxMessageBytes"`
	RetryMax         int      `default:"3" mapstructure:"retryMax"`
	RetryBackoffMs   int      `default:"100" mapstructure:"retryBackoffMs"`
	FlushFrequencyMs int      `default:"500" mapstructure:"flushFrequencyMs"`
	FlushMessages    int      `default:"100" mapstructure:"flushMessages"`
}

func (k KafkaProducerProperties) Prefix() string {
	return "app.kafka.producer"
}

func NewKafkaProducerProperties(loader config.Loader) (*KafkaProducerProperties, error) {
	props := KafkaProducerProperties{}
	err := loader.Bind(&props)
	return &props, err
}
