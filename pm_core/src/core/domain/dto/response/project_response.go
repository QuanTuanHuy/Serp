/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

type ProjectResponse struct {
	ID                 int64   `json:"id"`
	TenantID           int64   `json:"tenantId"`
	Name               string  `json:"name"`
	Key                string  `json:"key"`
	Description        *string `json:"description,omitempty"`
	Status             string  `json:"status"`
	Visibility         string  `json:"visibility"`
	StartDateMs        *int64  `json:"startDateMs,omitempty"`
	TargetEndDateMs    *int64  `json:"targetEndDateMs,omitempty"`
	NextItemNumber     int     `json:"nextItemNumber"`
	TotalWorkItems     int     `json:"totalWorkItems"`
	CompletedWorkItems int     `json:"completedWorkItems"`
	CreatedAt          int64   `json:"createdAt"`
	UpdatedAt          int64   `json:"updatedAt"`
}
