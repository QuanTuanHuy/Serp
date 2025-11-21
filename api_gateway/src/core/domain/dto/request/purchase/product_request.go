/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateProductRequest struct {
	Name           string  `json:"name"`
	Weight         float64 `json:"weight"`
	Height         float64 `json:"height"`
	Unit           string  `json:"unit"`
	CostPrice      int64   `json:"costPrice"`
	WholeSalePrice int64   `json:"wholeSalePrice"`
	RetailPrice    int64   `json:"retailPrice"`
	CategoryId     string  `json:"categoryId"`
	StatusId       string  `json:"statusId"`
	ImageId        string  `json:"imageId"`
	ExtraProps     string  `json:"extraProps"`
	VatRate        float32 `json:"vatRate"`
	SkuCode        string  `json:"skuCode"`
}

type UpdateProductRequest struct {
	Name           string  `json:"name"`
	Weight         float64 `json:"weight"`
	Height         float64 `json:"height"`
	Unit           string  `json:"unit"`
	CostPrice      int64   `json:"costPrice"`
	WholeSalePrice int64   `json:"wholeSalePrice"`
	RetailPrice    int64   `json:"retailPrice"`
	StatusId       string  `json:"statusId"`
	ImageId        string  `json:"imageId"`
	ExtraProps     string  `json:"extraProps"`
	VatRate        float32 `json:"vatRate"`
	SkuCode        string  `json:"skuCode"`
}

type GetProductParams struct {
	Page          *int    `form:"page"`
	Size          *int    `form:"size"`
	SortBy        *string `form:"sortBy"`
	SortDirection *string `form:"sortDirection"`
	Query         *string `form:"query"`
	StatusId      *string `form:"statusId"`
}
