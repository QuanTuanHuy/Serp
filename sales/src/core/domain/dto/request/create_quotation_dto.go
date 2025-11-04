/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateQuotationDTO struct {
	CustomerName  string  `json:"customerName" binding:"required"`
	CustomerEmail string  `json:"customerEmail" binding:"required,email"`
	TotalAmount   float64 `json:"totalAmount" binding:"required,min=0"`
	ValidUntil    *int64  `json:"validUntil"`
	Description   string  `json:"description"`
}
