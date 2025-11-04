/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

import "github.com/serp/sales/src/core/domain/enum"

type UpdateQuotationDTO struct {
	CustomerName  *string               `json:"customerName"`
	CustomerEmail *string               `json:"customerEmail" binding:"omitempty,email"`
	TotalAmount   *float64              `json:"totalAmount" binding:"omitempty,min=0"`
	ValidUntil    *int64                `json:"validUntil"`
	Status        *enum.QuotationStatus `json:"status"`
	Description   *string               `json:"description"`
}
