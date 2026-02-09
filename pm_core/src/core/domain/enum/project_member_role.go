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

func (r ProjectMemberRole) GetLevel() int {
	switch r {
	case RoleOwner:
		return 4
	case RoleAdmin:
		return 3
	case RoleMember:
		return 2
	case RoleViewer:
		return 1
	}
	return 0
}

func (r ProjectMemberRole) HasMinRole(minRole ProjectMemberRole) bool {
	return r.GetLevel() >= minRole.GetLevel()
}

func (r ProjectMemberRole) CanManageMembers() bool {
	return r.HasMinRole(RoleAdmin)
}

func (r ProjectMemberRole) CanManageSprints() bool {
	return r.HasMinRole(RoleAdmin)
}

func (r ProjectMemberRole) CanEditProject() bool {
	return r.HasMinRole(RoleAdmin)
}

func (r ProjectMemberRole) CanEditWorkItems() bool {
	return r.HasMinRole(RoleMember)
}

func (r ProjectMemberRole) IsViewOnly() bool {
	return r == RoleViewer
}
