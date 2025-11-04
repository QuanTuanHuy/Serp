/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package entity

import "github.com/serp/sales/src/core/domain/enum"

type QuotationEntity struct {
	BaseEntity
	QuotationNumber string               `json:"quotationNumber"`
	CustomerName    string               `json:"customerName"`
	CustomerEmail   string               `json:"customerEmail"`
	TotalAmount     float64              `json:"totalAmount"`
	ValidUntil      *int64               `json:"validUntil"`
	Status          enum.QuotationStatus `json:"status"`
	Description     string               `json:"description"`
	TenantID        int64                `json:"tenantId"`
	UserID          int64                `json:"userId"`
}
