/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package crm

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type ILeadClientPort interface {
	CreateLead(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	UpdateLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error)
	GetLeadByID(ctx context.Context, leadId int64) (*response.BaseResponse, error)
	GetLeads(ctx context.Context, page, size int) (*response.BaseResponse, error)
	FilterLeads(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	QualifyLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error)
	ConvertLead(ctx context.Context, leadId int64, payload map[string]any) (*response.BaseResponse, error)
	DeleteLead(ctx context.Context, leadId int64) (*response.BaseResponse, error)
}
