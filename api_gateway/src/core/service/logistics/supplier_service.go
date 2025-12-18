/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/logistics"
)

type ISupplierService interface {
	GetSuppliers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
	GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error)
}

type SupplierService struct {
	supplierClient port.ISupplierClientPort
}

func (s *SupplierService) GetSuppliers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
	res, err := s.supplierClient.GetSuppliers(ctx, page, size, sortBy, sortDirection, query, statusId)
	if err != nil {
		log.Error(ctx, "SupplierService: GetSuppliers error: ", err.Error())
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

func NewSupplierService(supplierClient port.ISupplierClientPort) ISupplierService {
	return &SupplierService{
		supplierClient: supplierClient,
	}
}
