/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type NotificationCategory string

const (
	CategorySystem NotificationCategory = "SYSTEM"
	CategoryEmail  NotificationCategory = "EMAIL"
	CategoryCRM    NotificationCategory = "CRM"
	CategoryPTM    NotificationCategory = "PTM"
)

func (nc NotificationCategory) String() string {
	return string(nc)
}
