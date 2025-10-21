/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateSubscriptionPlanRequest struct {
	PlanName     *string           `json:"planName"`
	Description  *string           `json:"description"`
	MonthlyPrice *float64          `json:"monthlyPrice"`
	YearlyPrice  *float64          `json:"yearlyPrice"`
	MaxUsers     *int              `json:"maxUsers"`
	TrialDays    *int              `json:"trialDays"`
	IsActive     *bool             `json:"isActive"`
	DisplayOrder *int              `json:"displayOrder"`
	Modules      []PlanModuleDto   `json:"modules"`
	Features     map[string]string `json:"features"`
}
