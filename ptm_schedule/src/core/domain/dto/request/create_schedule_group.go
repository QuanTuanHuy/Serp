package request

type CreateScheduleGroupDto struct {
	ProjectID    *int64    `json:"projectId"`
	GroupTaskID  *int64    `json:"groupTaskId"`
	Title        *string   `json:"title"`
	Priority     []*string `json:"priority" validate:"omitempty,dive,oneof=LOW MEDIUM HIGH STAR"`
	Status       *string   `json:"status" validate:"omitempty,oneof=TODO IN_PROGRESS DONE"`
	StartHour    *int32    `json:"startHour"`
	StartMinute  *int32    `json:"startMinute"`
	EndHour      *int32    `json:"endHour"`
	EndMinute    *int32    `json:"endMinute"`
	Duration     *float64  `json:"duration"`
	Repeat       []*string `json:"repeat"`
	IsNotify     *bool     `json:"isNotify"`
	ActiveStatus *string   `json:"activeStatus" validate:"omitempty,oneof=ACTIVE INACTIVE"`
	RotationDay  *int64    `json:"rotationDay"`
}
