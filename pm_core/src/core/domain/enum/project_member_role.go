/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type ProjectMemberRole string

const (
	RoleOwner  ProjectMemberRole = "OWNER"
	RoleAdmin  ProjectMemberRole = "ADMIN"
	RoleMember ProjectMemberRole = "MEMBER"
	RoleViewer ProjectMemberRole = "VIEWER"
)

func (r ProjectMemberRole) IsValid() bool {
	switch r {
	case RoleOwner, RoleAdmin, RoleMember, RoleViewer:
		return true
	}
	return false
}
