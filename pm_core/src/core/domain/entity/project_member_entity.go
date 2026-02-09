/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

type ProjectMemberEntity struct {
	BaseEntity

	ProjectID int64  `json:"projectId"`
	UserID    int64  `json:"userId"`
	Role      string `json:"role"`
	JoinedAt  int64  `json:"joinedAt"`
}
