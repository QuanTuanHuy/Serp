/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/pm-core/src/core/domain/enum"

type ProjectMemberEntity struct {
	BaseEntity

	ProjectID int64  `json:"projectId"`
	UserID    int64  `json:"userId"`
	Role      string `json:"role"`
	JoinedAt  int64  `json:"joinedAt"`
}

func NewProjectMemberEntity(projectID, userID int64, role enum.ProjectMemberRole, joinedAtMs int64) *ProjectMemberEntity {
	return &ProjectMemberEntity{
		ProjectID: projectID,
		UserID:    userID,
		Role:      string(role),
		JoinedAt:  joinedAtMs,
	}
}

func (m *ProjectMemberEntity) HasMinRole(minRole enum.ProjectMemberRole) bool {
	return enum.ProjectMemberRole(m.Role).HasMinRole(minRole)
}

func (m *ProjectMemberEntity) IsOwner() bool {
	return enum.ProjectMemberRole(m.Role) == enum.RoleOwner
}

func (m *ProjectMemberEntity) IsAdmin() bool {
	return m.HasMinRole(enum.RoleAdmin)
}

func (m *ProjectMemberEntity) CanEditProject() bool {
	return enum.ProjectMemberRole(m.Role).CanEditProject()
}

func (m *ProjectMemberEntity) CanManageMembers() bool {
	return enum.ProjectMemberRole(m.Role).CanManageMembers()
}

func (m *ProjectMemberEntity) CanEditWorkItems() bool {
	return enum.ProjectMemberRole(m.Role).CanEditWorkItems()
}

func (m *ProjectMemberEntity) CanManageSprints() bool {
	return enum.ProjectMemberRole(m.Role).CanManageSprints()
}

func (m *ProjectMemberEntity) IsViewOnly() bool {
	return enum.ProjectMemberRole(m.Role).IsViewOnly()
}
