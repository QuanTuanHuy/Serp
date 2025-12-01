/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package message

import (
	"encoding/json"
	"time"

	"github.com/golibs-starter/golib/log"
)

type KafkaMessage struct {
	Cmd          KafkaCommand `json:"cmd"`
	ErrorCode    ErrorCode    `json:"errorCode"`
	ErrorMessage ErrorMsg     `json:"errorMessage"`
	DisplayTime  int64        `json:"displayTime"`
	Data         any          `json:"data"`
	ReplyTopic   string       `json:"replyTopic,omitempty"`
	Source       string       `json:"source,omitempty"`
}

func CreateKafkaMessage(cmd KafkaCommand, errorCode ErrorCode, errorMessage ErrorMsg, data any) *KafkaMessage {
	return &KafkaMessage{
		Cmd:          cmd,
		ErrorCode:    errorCode,
		ErrorMessage: errorMessage,
		DisplayTime:  time.Now().UnixMilli(),
		Source:       "ptm-schedule",
		ReplyTopic:   "",
		Data:         data,
	}
}

func CreateKafkaMessageWithReplyTopic(cmd KafkaCommand, errorCode ErrorCode, errorMessage ErrorMsg, data any, replyTopic string) *KafkaMessage {
	return &KafkaMessage{
		Cmd:          cmd,
		ErrorCode:    errorCode,
		ErrorMessage: errorMessage,
		DisplayTime:  time.Now().UnixMilli(),
		Source:       "ptm-schedule",
		ReplyTopic:   replyTopic,
		Data:         data,
	}
}

func BindData(k *KafkaMessage, dst any) error {
	dataBytes, err := json.Marshal(k.Data)
	if err != nil {
		log.Error("Failed to marshal Kafka message data: ", err)
		return err
	}
	if err := json.Unmarshal(dataBytes, dst); err != nil {
		log.Error("Failed to unmarshal Kafka message data: ", err)
		return err
	}
	return nil
}

type KafkaCommand string

const (
	CREATE_TASK          KafkaCommand = "taskManagerCreateTask"
	UPDATE_TASK          KafkaCommand = "taskManagerUpdateTask"
	DELETE_TASK          KafkaCommand = "taskManagerDeleteTask"
	CREATE_SCHEDULE_TASK KafkaCommand = "scheduleManagerCreateScheduleTask"
)

type ErrorCode string

const (
	ErrorCodeSuccess ErrorCode = "00"
	ErrorCodeFailure ErrorCode = "01"
)

type ErrorMsg string

const (
	ErrorMsgSuccess ErrorMsg = "Successful"
	ErrorMsgFailure ErrorMsg = "Failed"
)
