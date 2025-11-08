/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type GetSubscriptionPlanParams struct {
	Page           *int    `json:"page,omitempty"`
	PageSize       *int    `json:"pageSize,omitempty"`
	SortBy         *string `json:"sortBy,omitempty"`
	SortDir        *string `json:"sortDir,omitempty"`
	IsCustom       *bool   `json:"isCustom,omitempty"`
	OrganizationID *int64  `json:"organizationId,omitempty"`
}
