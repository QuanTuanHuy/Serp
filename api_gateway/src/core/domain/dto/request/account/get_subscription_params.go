/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type GetSubscriptionParams struct {
	Page           *int    `json:"page,omitempty"`
	PageSize       *int    `json:"pageSize,omitempty"`
	SortBy         *string `json:"sortBy,omitempty"`
	SortDir        *string `json:"sortDir,omitempty"`
	OrganizationID *int64  `json:"organizationId,omitempty"`
	Status         *string `json:"status,omitempty"`
	BillingCycle   *string `json:"billingCycle,omitempty"`
}
