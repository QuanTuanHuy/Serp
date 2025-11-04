/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/sales/src/core/domain/entity"
	"gorm.io/gorm"
)

type IQuotationPort interface {
	CreateQuotation(ctx context.Context, tx *gorm.DB, quotation *entity.QuotationEntity) (*entity.QuotationEntity, error)
	UpdateQuotation(ctx context.Context, tx *gorm.DB, quotationID int64, quotation *entity.QuotationEntity) (*entity.QuotationEntity, error)
	GetQuotationByID(ctx context.Context, quotationID int64) (*entity.QuotationEntity, error)
	GetQuotationsByUserID(ctx context.Context, userID int64, tenantID int64) ([]*entity.QuotationEntity, error)
	DeleteQuotation(ctx context.Context, tx *gorm.DB, quotationID int64) error
}
