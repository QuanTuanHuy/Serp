/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateSubscriptionPlanRequest struct {
	PlanName       string            `json:"planName" binding:"required,max=100"`
	PlanCode       string            `json:"planCode" binding:"required,max=50"`
	Description    *string           `json:"description" binding:"omitempty,max=1000"`
	MonthlyPrice   float64           `json:"monthlyPrice" binding:"required,gte=0"`
	YearlyPrice    float64           `json:"yearlyPrice" binding:"required,gte=0"`
	MaxUsers       *int              `json:"maxUsers" binding:"omitempty,gte=1"`
	TrialDays      *int              `json:"trialDays" binding:"omitempty,gte=0,lte=365"`
	IsActive       bool              `json:"isActive"`
	IsCustom       bool              `json:"isCustom"`
	OrganizationId *int64            `json:"organizationId" binding:"omitempty"`
	DisplayOrder   int               `json:"displayOrder" binding:"gte=0"`
	Modules        []PlanModuleDto   `json:"modules,omitempty" binding:"dive"`
	Features       map[string]string `json:"features,omitempty"`
}
