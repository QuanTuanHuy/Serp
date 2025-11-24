/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ActiveStatus string

const (
	Active   ActiveStatus = "ACTIVE"
	Inactive ActiveStatus = "INACTIVE"
	Deleted  ActiveStatus = "DELETED"
)

func (s ActiveStatus) IsValid() bool {
	switch s {
	case Active, Inactive, Deleted:
		return true
	}
	return false
}

func (s ActiveStatus) IsActive() bool {
	return s == Active
}
