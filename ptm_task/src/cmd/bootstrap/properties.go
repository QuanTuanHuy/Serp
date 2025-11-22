/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package bootstrap

import (
	"github.com/serp/ptm-task/src/kernel/properties"
	"github.com/spf13/viper"
	"go.uber.org/zap"
)

func NewAppProperties(v *viper.Viper, logger *zap.Logger) (*properties.AppProperties, error) {
	var props properties.AppProperties
	if err := v.UnmarshalKey("app", &props); err != nil {
		return nil, err
	}
	logger.Info("Application properties loaded",
		zap.String("name", props.Name),
		zap.String("path", props.Path),
		zap.Int("port", props.Port),
	)
	// kafka properties
	logger.Info("Kafka Producer properties loaded",
		zap.String("bootstrapServers", props.Kafka.Producer.BootstrapServers[0]),
	)
	return &props, nil
}
