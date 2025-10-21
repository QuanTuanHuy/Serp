/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateModuleDto struct {
	Name             string  `json:"name"`
	Description      *string `json:"description"`
	KeycloakClientId string  `json:"keycloakClientId"`
	Category         *string `json:"category"`
	Icon             *string `json:"icon"`
	DisplayOrder     *int    `json:"displayOrder"`
	ModuleType       *string `json:"moduleType"`
	IsGlobal         *bool   `json:"isGlobal"`
	OrganizationId   *int64  `json:"organizationId"`
	IsFree           *bool   `json:"isFree"`
	PricingModel     *string `json:"pricingModel"`
	Status           *string `json:"status"`
}
