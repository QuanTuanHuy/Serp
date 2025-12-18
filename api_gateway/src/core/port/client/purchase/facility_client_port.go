/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IFacilityClientPort interface {
	CreateFacility(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateFacility(ctx context.Context, facilityId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
	GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
	GetFacilities(ctx context.Context, params *request.GetFacilityParams) (*response.BaseResponse, error)
}
