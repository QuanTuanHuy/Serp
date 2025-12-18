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

type ICustomerService interface {
	GetCustomers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
}

type CustomerService struct {
	customerClient port.ICustomerClientPort
}

func (c *CustomerService) GetCustomers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
	res, err := c.customerClient.GetCustomers(ctx, page, size, sortBy, sortDirection, query, statusId)
	if err != nil {
		log.Error(ctx, "CustomerService: GetCustomers error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewCustomerService(customerClient port.ICustomerClientPort) ICustomerService {
	return &CustomerService{
		customerClient: customerClient,
	}
}
