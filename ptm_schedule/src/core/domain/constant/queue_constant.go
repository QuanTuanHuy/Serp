/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

import "time"

const (
	DebounceWindow     = 300 * time.Millisecond
	DebounceWindowMs   = 300
	MaxDebounceWait    = 2 * time.Second
	MaxDebounceWaitMs  = 2000
	WorkerPollInterval = 5 * time.Second
	MaxPlansPerPoll    = 10
	MaxRetryCount      = 3
)

const (
	EntityTypeEvent = "EVENT"
	EntityTypeTask  = "TASK"
)
