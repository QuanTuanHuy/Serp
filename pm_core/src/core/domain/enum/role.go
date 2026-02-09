/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type KeycloakRole string

const (
	RoleUser    KeycloakRole = "PM_USER"
	RolePMAdmin KeycloakRole = "PM_ADMIN"
)

func (r KeycloakRole) IsValid() bool {
	switch r {
	case RoleUser, RolePMAdmin:
		return true
	}
	return false
}
