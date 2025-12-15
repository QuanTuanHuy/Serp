/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type NotificationType string

const (
	TypeInfo    NotificationType = "INFO"
	TypeSuccess NotificationType = "SUCCESS"
	TypeWarning NotificationType = "WARNING"
	TypeError   NotificationType = "ERROR"
)

func (nt NotificationType) String() string {
	return string(nt)
}
