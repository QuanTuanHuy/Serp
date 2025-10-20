/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type UpdateSubscriptionPlanRequest struct {
	Name                  *string  `json:"name"`
	Code                  *string  `json:"code"`
	Description           *string  `json:"description"`
	PlanType              *string  `json:"planType"`
	PriceMonthly          *float64 `json:"priceMonthly"`
	PriceYearly           *float64 `json:"priceYearly"`
	TrialDays             *int     `json:"trialDays"`
	MaxUsers              *int     `json:"maxUsers"`
	MaxStorage            *int64   `json:"maxStorage"`
	Features              *string  `json:"features"`
	IsActive              *bool    `json:"isActive"`
	DisplayOrder          *int     `json:"displayOrder"`
	IsRecommended         *bool    `json:"isRecommended"`
	CustomPricingEnabled  *bool    `json:"customPricingEnabled"`
	SupportLevel          *string  `json:"supportLevel"`
	AllowedModulesPerUser *int     `json:"allowedModulesPerUser"`
}
