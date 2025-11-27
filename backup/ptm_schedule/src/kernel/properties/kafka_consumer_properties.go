package properties

import (
	"github.com/golibs-starter/golib/config"
)

type KafkaConsumerProperties struct {
	BootstrapServers []string `default:"localhost:9092"`
	GroupID          string   `default:"task-manager-group"`
	AutoOffsetReset  string   `default:"earliest"`
	EnableAutoCommit bool     `default:"false"`
	SessionTimeoutMs int      `default:"30000"`
	HeartbeatMs      int      `default:"10000"`
	MaxPollRecords   int      `default:"100"`
	FetchMinBytes    int      `default:"1"`
	FetchMaxWaitMs   int      `default:"500"`
}

func (k KafkaConsumerProperties) Prefix() string {
	return "app.kafka.consumer"
}

func NewKafkaConsumerProperties(loader config.Loader) (*KafkaConsumerProperties, error) {
	props := KafkaConsumerProperties{}
	err := loader.Bind(&props)
	return &props, err
}
