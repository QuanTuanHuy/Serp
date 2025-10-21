/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateSubscriptionPlanRequest struct {
	PlanName       string            `json:"planName"`
	PlanCode       string            `json:"planCode"`
	Description    *string           `json:"description"`
	MonthlyPrice   *float64          `json:"monthlyPrice"`
	YearlyPrice    *float64          `json:"yearlyPrice"`
	MaxUsers       *int              `json:"maxUsers"`
	TrialDays      *int              `json:"trialDays"`
	IsActive       *bool             `json:"isActive"`
	IsCustom       *bool             `json:"isCustom"`
	OrganizationId *int64            `json:"organizationId"`
	DisplayOrder   *int              `json:"displayOrder"`
	Modules        []PlanModuleDto   `json:"modules"`
	Features       map[string]string `json:"features"`
}
