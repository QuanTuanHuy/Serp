/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package adapter

import (
	"context"
	"fmt"
	"time"

	"github.com/serp/sales/src/core/domain/entity"
	port "github.com/serp/sales/src/core/port/store"
	"github.com/serp/sales/src/infrastructure/store/mapper"
	"github.com/serp/sales/src/infrastructure/store/model"
	"gorm.io/gorm"
)

type QuotationStoreAdapter struct {
	db *gorm.DB
}

func (q *QuotationStoreAdapter) CreateQuotation(ctx context.Context, tx *gorm.DB, quotation *entity.QuotationEntity) (*entity.QuotationEntity, error) {
	// Generate unique quotation number
	quotation.QuotationNumber = fmt.Sprintf("QUO-%d-%d", time.Now().Unix(), quotation.UserID)

	quotationModel := mapper.ToQuotationModel(quotation)
	if err := tx.WithContext(ctx).Create(quotationModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToQuotationEntity(quotationModel), nil
}

func (q *QuotationStoreAdapter) UpdateQuotation(ctx context.Context, tx *gorm.DB, quotationID int64, quotation *entity.QuotationEntity) (*entity.QuotationEntity, error) {
	quotationModel := mapper.ToQuotationModel(quotation)
	if err := tx.WithContext(ctx).
		Model(&model.QuotationModel{}).
		Where("id = ?", quotationID).
		Updates(quotationModel).Error; err != nil {
		return nil, err
	}
	return mapper.ToQuotationEntity(quotationModel), nil
}

func (q *QuotationStoreAdapter) GetQuotationByID(ctx context.Context, quotationID int64) (*entity.QuotationEntity, error) {
	var quotation model.QuotationModel
	if err := q.db.WithContext(ctx).Where("id = ?", quotationID).
		First(&quotation).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, err
	}
	return mapper.ToQuotationEntity(&quotation), nil
}

func (q *QuotationStoreAdapter) GetQuotationsByUserID(ctx context.Context, userID int64, tenantID int64) ([]*entity.QuotationEntity, error) {
	var quotations []*model.QuotationModel
	if err := q.db.WithContext(ctx).
		Where("user_id = ? AND tenant_id = ?", userID, tenantID).
		Order("created_at DESC").
		Find(&quotations).Error; err != nil {
		return nil, err
	}
	return mapper.ToQuotationEntityList(quotations), nil
}

func (q *QuotationStoreAdapter) DeleteQuotation(ctx context.Context, tx *gorm.DB, quotationID int64) error {
	if err := tx.WithContext(ctx).
		Where("id = ?", quotationID).
		Delete(&model.QuotationModel{}).Error; err != nil {
		return err
	}
	return nil
}

func NewQuotationStoreAdapter(db *gorm.DB) port.IQuotationPort {
	return &QuotationStoreAdapter{db: db}
}
