/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"
	"errors"

	"github.com/serp/sales/src/core/domain/constant"
	"github.com/serp/sales/src/core/domain/dto/request"
	"github.com/serp/sales/src/core/domain/entity"
	"github.com/serp/sales/src/core/domain/mapper"
	port "github.com/serp/sales/src/core/port/store"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type IQuotationService interface {
	CreateQuotation(ctx context.Context, tx *gorm.DB, userID int64, tenantID int64, request *request.CreateQuotationDTO) (*entity.QuotationEntity, error)
	UpdateQuotation(ctx context.Context, tx *gorm.DB, userID int64, tenantID int64, quotationID int64, request *request.UpdateQuotationDTO) (*entity.QuotationEntity, error)
	GetQuotationByID(ctx context.Context, quotationID int64) (*entity.QuotationEntity, error)
	GetQuotationsByUserID(ctx context.Context, userID int64, tenantID int64) ([]*entity.QuotationEntity, error)
	DeleteQuotation(ctx context.Context, tx *gorm.DB, userID int64, tenantID int64, quotationID int64) error
}

type QuotationService struct {
	quotationPort port.IQuotationPort
	logger        *zap.Logger
}

func (q *QuotationService) CreateQuotation(ctx context.Context, tx *gorm.DB, userID int64, tenantID int64, request *request.CreateQuotationDTO) (*entity.QuotationEntity, error) {
	quotation := mapper.ToQuotationEntity(request)
	quotation.UserID = userID
	quotation.TenantID = tenantID

	quotation, err := q.quotationPort.CreateQuotation(ctx, tx, quotation)
	if err != nil {
		q.logger.Error("Failed to create quotation", zap.Error(err))
		return nil, err
	}
	return quotation, nil
}

func (q *QuotationService) UpdateQuotation(ctx context.Context, tx *gorm.DB, userID int64, tenantID int64, quotationID int64, request *request.UpdateQuotationDTO) (*entity.QuotationEntity, error) {
	quotation, err := q.GetQuotationByUserIDAndQuotationID(ctx, userID, tenantID, quotationID)
	if err != nil {
		return nil, err
	}

	quotation = mapper.UpdateQuotationMapper(quotation, request)
	quotation, err = q.quotationPort.UpdateQuotation(ctx, tx, quotationID, quotation)
	if err != nil {
		q.logger.Error("Failed to update quotation", zap.Int64("quotationID", quotationID), zap.Error(err))
		return nil, err
	}

	return quotation, nil
}

func (q *QuotationService) GetQuotationByID(ctx context.Context, quotationID int64) (*entity.QuotationEntity, error) {
	quotation, err := q.quotationPort.GetQuotationByID(ctx, quotationID)
	if err != nil {
		q.logger.Error("Failed to get quotation by ID", zap.Int64("quotationID", quotationID), zap.Error(err))
		return nil, err
	}
	if quotation == nil {
		return nil, errors.New(constant.QuotationNotFound)
	}
	return quotation, nil
}

func (q *QuotationService) GetQuotationByUserIDAndQuotationID(ctx context.Context, userID int64, tenantID int64, quotationID int64) (*entity.QuotationEntity, error) {
	quotation, err := q.quotationPort.GetQuotationByID(ctx, quotationID)
	if err != nil {
		return nil, err
	}
	if quotation == nil {
		return nil, errors.New(constant.QuotationNotFound)
	}
	if quotation.UserID != userID || quotation.TenantID != tenantID {
		return nil, errors.New(constant.GetQuotationForbidden)
	}
	return quotation, nil
}

func (q *QuotationService) GetQuotationsByUserID(ctx context.Context, userID int64, tenantID int64) ([]*entity.QuotationEntity, error) {
	quotations, err := q.quotationPort.GetQuotationsByUserID(ctx, userID, tenantID)
	if err != nil {
		q.logger.Error("Failed to get quotations by user ID", zap.Int64("userID", userID), zap.Error(err))
		return nil, err
	}
	return quotations, nil
}

func (q *QuotationService) DeleteQuotation(ctx context.Context, tx *gorm.DB, userID int64, tenantID int64, quotationID int64) error {
	_, err := q.GetQuotationByUserIDAndQuotationID(ctx, userID, tenantID, quotationID)
	if err != nil {
		return err
	}

	err = q.quotationPort.DeleteQuotation(ctx, tx, quotationID)
	if err != nil {
		q.logger.Error("Failed to delete quotation", zap.Int64("quotationID", quotationID), zap.Error(err))
		return err
	}
	return nil
}

func NewQuotationService(quotationPort port.IQuotationPort, logger *zap.Logger) IQuotationService {
	return &QuotationService{
		quotationPort: quotationPort,
		logger:        logger,
	}
}
