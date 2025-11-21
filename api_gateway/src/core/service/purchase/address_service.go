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

type IAddressService interface {
	CreateAddress(ctx context.Context, req *request.CreateAddressRequest) (*response.BaseResponse, error)
	UpdateAddress(ctx context.Context, addressId string, req *request.UpdateAddressRequest) (*response.BaseResponse, error)
}

type AddressService struct {
	addressClient port.IAddressClientPort
}

func (a *AddressService) CreateAddress(ctx context.Context, req *request.CreateAddressRequest) (*response.BaseResponse, error) {
	res, err := a.addressClient.CreateAddress(ctx, req)
	if err != nil {
		log.Error(ctx, "AddressService: CreateAddress error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (a *AddressService) UpdateAddress(ctx context.Context, addressId string, req *request.UpdateAddressRequest) (*response.BaseResponse, error) {
	res, err := a.addressClient.UpdateAddress(ctx, addressId, req)
	if err != nil {
		log.Error(ctx, "AddressService: UpdateAddress error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewAddressService(addressClient port.IAddressClientPort) IAddressService {
	return &AddressService{
		addressClient: addressClient,
	}
}
