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

type ISupplierService interface {
	CreateSupplier(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateSupplier(ctx context.Context, supplierId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error)
	GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error)
	GetSuppliers(ctx context.Context, params *request.GetSupplierParams) (*response.BaseResponse, error)
}

type SupplierService struct {
	supplierClient port.ISupplierClientPort
}

func (s *SupplierService) CreateSupplier(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := s.supplierClient.CreateSupplier(ctx, req)
	if err != nil {
		log.Error(ctx, "SupplierService: CreateSupplier error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SupplierService) UpdateSupplier(ctx context.Context, supplierId string, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := s.supplierClient.UpdateSupplier(ctx, supplierId, req)
	if err != nil {
		log.Error(ctx, "SupplierService: UpdateSupplier error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SupplierService) DeleteSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error) {
	res, err := s.supplierClient.DeleteSupplier(ctx, supplierId)
	if err != nil {
		log.Error(ctx, "SupplierService: DeleteSupplier error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SupplierService) GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error) {
	res, err := s.supplierClient.GetSupplier(ctx, supplierId)
	if err != nil {
		log.Error(ctx, "SupplierService: GetSupplier error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SupplierService) GetSuppliers(ctx context.Context, params *request.GetSupplierParams) (*response.BaseResponse, error) {
	res, err := s.supplierClient.GetSuppliers(ctx, params)
	if err != nil {
		log.Error(ctx, "SupplierService: GetSuppliers error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewSupplierService(supplierClient port.ISupplierClientPort) ISupplierService {
	return &SupplierService{
		supplierClient: supplierClient,
	}
}
