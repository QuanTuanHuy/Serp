/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"
	"errors"

	"github.com/serp/sales/src/core/domain/constant"
	"github.com/serp/sales/src/core/domain/dto/request"
	"github.com/serp/sales/src/core/domain/entity"
	"github.com/serp/sales/src/core/service"
	"go.uber.org/zap"
	"gorm.io/gorm"
)

type IQuotationUseCase interface {
	CreateQuotation(ctx context.Context, userID int64, tenantID int64, request *request.CreateQuotationDTO) (*entity.QuotationEntity, error)
	UpdateQuotation(ctx context.Context, userID int64, tenantID int64, quotationID int64, request *request.UpdateQuotationDTO) (*entity.QuotationEntity, error)
	GetQuotationByID(ctx context.Context, userID int64, tenantID int64, quotationID int64) (*entity.QuotationEntity, error)
	GetQuotationsByUserID(ctx context.Context, userID int64, tenantID int64) ([]*entity.QuotationEntity, error)
	DeleteQuotation(ctx context.Context, userID int64, tenantID int64, quotationID int64) error
}

type QuotationUseCase struct {
	quotationService service.IQuotationService
	txService        service.ITransactionService
	logger           *zap.Logger
}

func (q *QuotationUseCase) CreateQuotation(ctx context.Context, userID int64, tenantID int64, request *request.CreateQuotationDTO) (*entity.QuotationEntity, error) {
	result, err := q.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		quotation, err := q.quotationService.CreateQuotation(ctx, tx, userID, tenantID, request)
		if err != nil {
			return nil, err
		}
		return quotation, nil
	})
	if err != nil {
		return nil, err
	}

	return result.(*entity.QuotationEntity), nil
}

func (q *QuotationUseCase) UpdateQuotation(ctx context.Context, userID int64, tenantID int64, quotationID int64, request *request.UpdateQuotationDTO) (*entity.QuotationEntity, error) {
	result, err := q.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		quotation, err := q.quotationService.UpdateQuotation(ctx, tx, userID, tenantID, quotationID, request)
		if err != nil {
			return nil, err
		}
		return quotation, nil
	})
	if err != nil {
		return nil, err
	}

	return result.(*entity.QuotationEntity), nil
}

func (q *QuotationUseCase) GetQuotationByID(ctx context.Context, userID int64, tenantID int64, quotationID int64) (*entity.QuotationEntity, error) {
	quotation, err := q.quotationService.GetQuotationByID(ctx, quotationID)
	if err != nil {
		return nil, err
	}
	if quotation.UserID != userID || quotation.TenantID != tenantID {
		q.logger.Error("User does not have permission to access quotation",
			zap.Int64("userID", userID),
			zap.Int64("tenantID", tenantID),
			zap.Int64("quotationID", quotationID))
		return nil, errors.New(constant.GetQuotationForbidden)
	}
	return quotation, nil
}

func (q *QuotationUseCase) GetQuotationsByUserID(ctx context.Context, userID int64, tenantID int64) ([]*entity.QuotationEntity, error) {
	return q.quotationService.GetQuotationsByUserID(ctx, userID, tenantID)
}

func (q *QuotationUseCase) DeleteQuotation(ctx context.Context, userID int64, tenantID int64, quotationID int64) error {
	return q.txService.ExecuteInTransaction(ctx, func(tx *gorm.DB) error {
		err := q.quotationService.DeleteQuotation(ctx, tx, userID, tenantID, quotationID)
		if err != nil {
			return err
		}
		return nil
	})
}

func NewQuotationUseCase(
	quotationService service.IQuotationService,
	txService service.ITransactionService,
	logger *zap.Logger) IQuotationUseCase {
	return &QuotationUseCase{
		quotationService: quotationService,
		txService:        txService,
		logger:           logger,
	}
}
