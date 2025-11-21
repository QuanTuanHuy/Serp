/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateSupplierRequest struct {
	Name        string  `json:"name"`
	Email       string  `json:"email"`
	Phone       string  `json:"phone"`
	StatusId    string  `json:"statusId"`
	AddressType string  `json:"addressType"`
	Latitude    float32 `json:"latitude"`
	Longitude   float32 `json:"longitude"`
	IsDefault   bool    `json:"isDefault"`
	FullAddress string  `json:"fullAddress"`
}

type UpdateSupplierRequest struct {
	Name     string `json:"name"`
	Email    string `json:"email"`
	Phone    string `json:"phone"`
	StatusId string `json:"statusId"`
}

type GetSupplierParams struct {
	Page          *int    `form:"page"`
	Size          *int    `form:"size"`
	SortBy        *string `form:"sortBy"`
	SortDirection *string `form:"sortDirection"`
	Query         *string `form:"query"`
	StatusId      *string `form:"statusId"`
}
