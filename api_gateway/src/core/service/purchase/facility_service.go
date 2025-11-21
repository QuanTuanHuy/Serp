/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/purchase"
)

type IFacilityService interface {
	CreateFacility(ctx context.Context, req *request.CreateFacilityRequest) (*response.BaseResponse, error)
	UpdateFacility(ctx context.Context, facilityId string, req *request.UpdateFacilityRequest) (*response.BaseResponse, error)
	DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
	GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
	GetFacilities(ctx context.Context, params *request.GetFacilityParams) (*response.BaseResponse, error)
}

type FacilityService struct {
	facilityClient port.IFacilityClientPort
}

func (f *FacilityService) CreateFacility(ctx context.Context, req *request.CreateFacilityRequest) (*response.BaseResponse, error) {
	res, err := f.facilityClient.CreateFacility(ctx, req)
	if err != nil {
		log.Error(ctx, "FacilityService: CreateFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) UpdateFacility(ctx context.Context, facilityId string, req *request.UpdateFacilityRequest) (*response.BaseResponse, error) {
	res, err := f.facilityClient.UpdateFacility(ctx, facilityId, req)
	if err != nil {
		log.Error(ctx, "FacilityService: UpdateFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	res, err := f.facilityClient.DeleteFacility(ctx, facilityId)
	if err != nil {
		log.Error(ctx, "FacilityService: DeleteFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	res, err := f.facilityClient.GetFacility(ctx, facilityId)
	if err != nil {
		log.Error(ctx, "FacilityService: GetFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) GetFacilities(ctx context.Context, params *request.GetFacilityParams) (*response.BaseResponse, error) {
	res, err := f.facilityClient.GetFacilities(ctx, params)
	if err != nil {
		log.Error(ctx, "FacilityService: GetFacilities error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewFacilityService(facilityClient port.IFacilityClientPort) IFacilityService {
	return &FacilityService{
		facilityClient: facilityClient,
	}
}
