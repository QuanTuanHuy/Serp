/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateFacilityRequest struct {
	Name             string  `json:"name"`
	Phone            string  `json:"phone"`
	StatusId         string  `json:"statusId"`
	PostalCode       string  `json:"postalCode"`
	Length           float32 `json:"length"`
	Weight           float32 `json:"weight"`
	Height           float32 `json:"height"`
	AddressType      string  `json:"addressType"`
	Latitude         float32 `json:"latitude"`
	Longitude        float32 `json:"longitude"`
	IsAddressDefault bool    `json:"isAddressDefault"`
	FullAddress      string  `json:"fullAddress"`
}

type UpdateFacilityRequest struct {
	Name       string  `json:"name"`
	IsDefault  bool    `json:"isDefault"`
	StatusId   string  `json:"statusId"`
	Phone      string  `json:"phone"`
	PostalCode string  `json:"postalCode"`
	Length     float32 `json:"length"`
	Weight     float32 `json:"weight"`
	Height     float32 `json:"height"`
}

type GetFacilityParams struct {
	Page          *int    `form:"page"`
	Size          *int    `form:"size"`
	SortBy        *string `form:"sortBy"`
	SortDirection *string `form:"sortDirection"`
	Query         *string `form:"query"`
	StatusId      *string `form:"statusId"`
}
