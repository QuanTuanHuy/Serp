/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package model

import "time"

type QuotationModel struct {
	BaseModel
	QuotationNumber string     `gorm:"uniqueIndex;not null" json:"quotationNumber"`
	CustomerName    string     `gorm:"not null" json:"customerName"`
	CustomerEmail   string     `gorm:"not null" json:"customerEmail"`
	TotalAmount     float64    `gorm:"not null;type:decimal(15,2)" json:"totalAmount"`
	ValidUntil      *time.Time `json:"validUntil"`
	Status          string     `gorm:"not null;default:'DRAFT'" json:"status"`
	Description     string     `gorm:"type:text" json:"description"`
	TenantID        int64      `gorm:"not null;index" json:"tenantId"`
	UserID          int64      `gorm:"not null;index" json:"userId"`
}

func (QuotationModel) TableName() string {
	return "quotations"
}
