/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package mapper

import (
	"time"

	"github.com/serp/sales/src/core/domain/entity"
	"github.com/serp/sales/src/core/domain/enum"
	"github.com/serp/sales/src/infrastructure/store/model"
)

func ToQuotationModel(quotation *entity.QuotationEntity) *model.QuotationModel {
	if quotation == nil {
		return nil
	}

	var validUntil *time.Time
	if quotation.ValidUntil != nil {
		validTime := time.UnixMilli(*quotation.ValidUntil)
		validUntil = &validTime
	}

	return &model.QuotationModel{
		BaseModel: model.BaseModel{
			ID: quotation.ID,
		},
		QuotationNumber: quotation.QuotationNumber,
		CustomerName:    quotation.CustomerName,
		CustomerEmail:   quotation.CustomerEmail,
		TotalAmount:     quotation.TotalAmount,
		ValidUntil:      validUntil,
		Status:          string(quotation.Status),
		Description:     quotation.Description,
		TenantID:        quotation.TenantID,
		UserID:          quotation.UserID,
	}
}

func ToQuotationEntity(quotationModel *model.QuotationModel) *entity.QuotationEntity {
	if quotationModel == nil {
		return nil
	}

	var validUntil *int64
	if quotationModel.ValidUntil != nil {
		validUnix := quotationModel.ValidUntil.UnixMilli()
		validUntil = &validUnix
	}

	return &entity.QuotationEntity{
		BaseEntity: entity.BaseEntity{
			ID:        quotationModel.ID,
			CreatedAt: quotationModel.CreatedAt.UnixMilli(),
			UpdatedAt: quotationModel.UpdatedAt.UnixMilli(),
		},
		QuotationNumber: quotationModel.QuotationNumber,
		CustomerName:    quotationModel.CustomerName,
		CustomerEmail:   quotationModel.CustomerEmail,
		TotalAmount:     quotationModel.TotalAmount,
		ValidUntil:      validUntil,
		Status:          enum.QuotationStatus(quotationModel.Status),
		Description:     quotationModel.Description,
		TenantID:        quotationModel.TenantID,
		UserID:          quotationModel.UserID,
	}
}

func ToQuotationEntityList(quotationModels []*model.QuotationModel) []*entity.QuotationEntity {
	if quotationModels == nil {
		return nil
	}
	quotations := make([]*entity.QuotationEntity, len(quotationModels))
	for i, quotationModel := range quotationModels {
		quotations[i] = ToQuotationEntity(quotationModel)
	}
	return quotations
}
