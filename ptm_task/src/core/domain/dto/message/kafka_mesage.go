/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package message

import "time"

type KafkaMessage struct {
	Cmd          KafkaCommand `json:"cmd"`
	ErrorCode    ErrorCode    `json:"errorCode"`
	ErrorMessage ErrorMsg     `json:"errorMessage"`
	DisplayTime  int64        `json:"displayTime"`
	Data         any          `json:"data"`
}

func CreateKafkaMessage(cmd KafkaCommand, errorCode ErrorCode, errorMessage ErrorMsg, data any) *KafkaMessage {
	return &KafkaMessage{
		Cmd:          cmd,
		ErrorCode:    errorCode,
		ErrorMessage: errorMessage,
		DisplayTime:  time.Now().UnixMilli(),
		Data:         data,
	}
}

type KafkaCommand string

const (
	CREATE_TASK KafkaCommand = "taskManagerCreateTask"
	UPDATE_TASK KafkaCommand = "taskManagerUpdateTask"
	DELETE_TASK KafkaCommand = "taskManagerDeleteTask"
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
