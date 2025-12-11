/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package crm

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/crm"
)

type IOpportunityService interface {
	CreateOpportunity(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	UpdateOpportunity(ctx context.Context, opportunityId int64, payload map[string]any) (*response.BaseResponse, error)
	GetOpportunityByID(ctx context.Context, opportunityId int64) (*response.BaseResponse, error)
	GetOpportunities(ctx context.Context, page, size int) (*response.BaseResponse, error)
	FilterOpportunities(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)

	ChangeStage(ctx context.Context, opportunityId int64, newStage string) (*response.BaseResponse, error)
	CloseAsWon(ctx context.Context, opportunityId int64) (*response.BaseResponse, error)
	CloseAsLost(ctx context.Context, opportunityId int64, lostReason string) (*response.BaseResponse, error)
	DeleteOpportunity(ctx context.Context, opportunityId int64) (*response.BaseResponse, error)
}

type OpportunityService struct {
	opportunityClient port.IOpportunityClientPort
}

func (o *OpportunityService) CreateOpportunity(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.CreateOpportunity(ctx, payload)
	if err != nil {
		log.Error(ctx, "OpportunityService: CreateOpportunity error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) UpdateOpportunity(ctx context.Context, opportunityId int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.UpdateOpportunity(ctx, opportunityId, payload)
	if err != nil {
		log.Error(ctx, "OpportunityService: UpdateOpportunity error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) GetOpportunityByID(ctx context.Context, opportunityId int64) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.GetOpportunityByID(ctx, opportunityId)
	if err != nil {
		log.Error(ctx, "OpportunityService: GetOpportunityByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) GetOpportunities(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.GetOpportunities(ctx, page, size)
	if err != nil {
		log.Error(ctx, "OpportunityService: GetOpportunities error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) FilterOpportunities(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.FilterOpportunities(ctx, payload)
	if err != nil {
		log.Error(ctx, "OpportunityService: FilterOpportunities error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) ChangeStage(ctx context.Context, opportunityId int64, newStage string) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.ChangeStage(ctx, opportunityId, newStage)
	if err != nil {
		log.Error(ctx, "OpportunityService: ChangeStage error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) CloseAsWon(ctx context.Context, opportunityId int64) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.CloseAsWon(ctx, opportunityId)
	if err != nil {
		log.Error(ctx, "OpportunityService: CloseAsWon error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) CloseAsLost(ctx context.Context, opportunityId int64, lostReason string) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.CloseAsLost(ctx, opportunityId, lostReason)
	if err != nil {
		log.Error(ctx, "OpportunityService: CloseAsLost error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OpportunityService) DeleteOpportunity(ctx context.Context, opportunityId int64) (*response.BaseResponse, error) {
	res, err := o.opportunityClient.DeleteOpportunity(ctx, opportunityId)
	if err != nil {
		log.Error(ctx, "OpportunityService: DeleteOpportunity error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewOpportunityService(opportunityClient port.IOpportunityClientPort) IOpportunityService {
	return &OpportunityService{opportunityClient: opportunityClient}
}
