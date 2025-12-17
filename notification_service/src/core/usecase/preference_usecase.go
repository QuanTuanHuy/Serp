/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package usecase

import (
	"context"

	"github.com/serp/notification-service/src/core/domain/dto/request"
	"github.com/serp/notification-service/src/core/domain/dto/response"
	"github.com/serp/notification-service/src/core/domain/entity"
	"github.com/serp/notification-service/src/core/domain/mapper"
	"github.com/serp/notification-service/src/core/service"
	"gorm.io/gorm"
)

type IPreferenceUseCase interface {
	GetPreferences(ctx context.Context, userID int64) (*response.PreferenceResponse, error)
	UpdatePreferences(ctx context.Context, userID int64, req *request.UpdatePreferenceRequest) (*response.PreferenceResponse, error)
}

type PreferenceUseCase struct {
	preferenceService service.IPreferenceService
	txService         service.ITransactionService
}

func (p *PreferenceUseCase) GetPreferences(ctx context.Context, userID int64) (*response.PreferenceResponse, error) {
	result, err := p.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		pref, err := p.preferenceService.GetOrCreate(ctx, tx, userID)
		if err != nil {
			return nil, err
		}
		return pref, nil
	})
	if err != nil {
		return nil, err
	}
	return mapper.PreferenceEntityToResponse(result.(*entity.NotificationPreferenceEntity)), nil
}

func (p *PreferenceUseCase) UpdatePreferences(ctx context.Context, userID int64, req *request.UpdatePreferenceRequest) (*response.PreferenceResponse, error) {
	result, err := p.txService.ExecuteInTransactionWithResult(ctx, func(tx *gorm.DB) (any, error) {
		updatedPref, err := p.preferenceService.Update(ctx, tx, userID, req)
		if err != nil {
			return nil, err
		}
		return updatedPref, nil
	})
	if err != nil {
		return nil, err
	}
	return mapper.PreferenceEntityToResponse(result.(*entity.NotificationPreferenceEntity)), nil
}

func NewPreferenceUseCase(
	preferenceService service.IPreferenceService,
	txService service.ITransactionService,
) IPreferenceUseCase {
	return &PreferenceUseCase{
		preferenceService: preferenceService,
	}
}
