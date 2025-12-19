/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package utils

import (
	"encoding/json"
	"fmt"

	"github.com/serp/notification-service/src/core/domain/dto/message"
)

func BindKafkaMessageData(msg *message.BaseKafkaMessage, out any) error {
	if msg == nil {
		return fmt.Errorf("kafka message is nil")
	}
	if out == nil {
		return fmt.Errorf("output is nil")
	}
	if len(msg.Data) == 0 {
		return fmt.Errorf("kafka message data is empty")
	}
	if err := json.Unmarshal(msg.Data, out); err != nil {
		return fmt.Errorf("failed to unmarshal kafka message data: %w", err)
	}
	return nil
}
