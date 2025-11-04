/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"github.com/serp/sales/src/core/domain/dto/request"
	"github.com/serp/sales/src/core/domain/entity"
	"github.com/serp/sales/src/core/domain/enum"
)

func ToQuotationEntity(req *request.CreateQuotationDTO) *entity.QuotationEntity {
	if req == nil {
		return nil
	}

	return &entity.QuotationEntity{
		CustomerName:  req.CustomerName,
		CustomerEmail: req.CustomerEmail,
		TotalAmount:   req.TotalAmount,
		ValidUntil:    req.ValidUntil,
		Description:   req.Description,
		Status:        enum.Draft,
	}
}

func UpdateQuotationMapper(quotation *entity.QuotationEntity, req *request.UpdateQuotationDTO) *entity.QuotationEntity {
	if req.CustomerName != nil {
		quotation.CustomerName = *req.CustomerName
	}
	if req.CustomerEmail != nil {
		quotation.CustomerEmail = *req.CustomerEmail
	}
	if req.TotalAmount != nil {
		quotation.TotalAmount = *req.TotalAmount
	}
	if req.ValidUntil != nil {
		quotation.ValidUntil = req.ValidUntil
	}
	if req.Status != nil {
		quotation.Status = *req.Status
	}
	if req.Description != nil {
		quotation.Description = *req.Description
	}
	return quotation
}
