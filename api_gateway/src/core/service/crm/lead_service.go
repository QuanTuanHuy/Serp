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

type ILeadService interface {
	CreateLead(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	UpdateLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error)
	GetLeadByID(ctx context.Context, leadId int64) (*response.BaseResponse, error)
	GetLeads(ctx context.Context, page, size int) (*response.BaseResponse, error)
	FilterLeads(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	QualifyLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error)
	ConvertLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteLead(ctx context.Context, leadId int64) (*response.BaseResponse, error)
}

type LeadService struct {
	leadClient port.ILeadClientPort
}

func (l *LeadService) CreateLead(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := l.leadClient.CreateLead(ctx, payload)
	if err != nil {
		log.Error(ctx, "LeadService: CreateLead error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (l *LeadService) UpdateLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := l.leadClient.UpdateLead(ctx, leadId, payload)
	if err != nil {
		log.Error(ctx, "LeadService: UpdateLead error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (l *LeadService) GetLeadByID(ctx context.Context, leadId int64) (*response.BaseResponse, error) {
	res, err := l.leadClient.GetLeadByID(ctx, leadId)
	if err != nil {
		log.Error(ctx, "LeadService: GetLeadByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (l *LeadService) GetLeads(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	res, err := l.leadClient.GetLeads(ctx, page, size)
	if err != nil {
		log.Error(ctx, "LeadService: GetLeads error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (l *LeadService) FilterLeads(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := l.leadClient.FilterLeads(ctx, payload)
	if err != nil {
		log.Error(ctx, "LeadService: FilterLeads error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (l *LeadService) QualifyLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := l.leadClient.QualifyLead(ctx, leadId, payload)
	if err != nil {
		log.Error(ctx, "LeadService: QualifyLead error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (l *LeadService) ConvertLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := l.leadClient.ConvertLead(ctx, leadId, payload)
	if err != nil {
		log.Error(ctx, "LeadService: ConvertLead error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (l *LeadService) DeleteLead(ctx context.Context, leadId int64) (*response.BaseResponse, error) {
	res, err := l.leadClient.DeleteLead(ctx, leadId)
	if err != nil {
		log.Error(ctx, "LeadService: DeleteLead error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewLeadService(leadClient port.ILeadClientPort) ILeadService {
	return &LeadService{leadClient: leadClient}
}
