/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type GetOrganizationParams struct {
	Search           *string `json:"search,omitempty"`
	Status           *string `json:"status,omitempty"`
	OrganizationType *string `json:"type,omitempty"`
	Page             *int    `json:"page,omitempty"`
	PageSize         *int    `json:"pageSize,omitempty"`
}
