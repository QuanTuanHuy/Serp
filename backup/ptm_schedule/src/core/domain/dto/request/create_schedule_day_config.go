/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateScheduleDayConfigDTO struct {
	Work     float64                        `json:"work" validate:"required,min=0,max=24"`
	Eat      float64                        `json:"eat" validate:"required,min=0,max=24"`
	Relax    float64                        `json:"relax" validate:"required,min=0,max=24"`
	Travel   float64                        `json:"travel" validate:"required,min=0,max=24"`
	Sleep    float64                        `json:"sleep" validate:"required,min=0,max=24"`
	Schedule map[int][]CreateTimeBubblesDTO `json:"timeBubbles" validate:"required,dive,keys,min=0,max=6,endkeys,dive"`
}

type CreateTimeBubblesDTO struct {
	StartTime string `json:"startTime" validate:"required,datetime=15:04:05"`
	EndTime   string `json:"endTime" validate:"required,datetime=15:04:05,gtfield=StartTime"`
	Tag       string `json:"tag" validate:"required,oneof=WORK EAT SLEEP TRAVEL RELAX"`
}
