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

type ICustomerService interface {
	CreateCustomer(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	UpdateCustomer(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error)
	GetCustomerByID(ctx context.Context, customerId int64) (*response.BaseResponse, error)
	GetCustomers(ctx context.Context, page, size int) (*response.BaseResponse, error)
	FilterCustomers(ctx context.Context, payload map[string]any) (*response.BaseResponse, error)
	DeleteCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error)
}

type CustomerService struct {
	customerClient port.ICustomerClientPort
}

func (c *CustomerService) CreateCustomer(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := c.customerClient.CreateCustomer(ctx, payload)
	if err != nil {
		log.Error(ctx, "CustomerService: CreateCustomer error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CustomerService) UpdateCustomer(ctx context.Context, customerId int64, payload map[string]any) (*response.BaseResponse, error) {
	res, err := c.customerClient.UpdateCustomer(ctx, customerId, payload)
	if err != nil {
		log.Error(ctx, "CustomerService: UpdateCustomer error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CustomerService) GetCustomerByID(ctx context.Context, customerId int64) (*response.BaseResponse, error) {
	res, err := c.customerClient.GetCustomerByID(ctx, customerId)
	if err != nil {
		log.Error(ctx, "CustomerService: GetCustomerByID error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CustomerService) GetCustomers(ctx context.Context, page, size int) (*response.BaseResponse, error) {
	res, err := c.customerClient.GetCustomers(ctx, page, size)
	if err != nil {
		log.Error(ctx, "CustomerService: GetCustomers error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CustomerService) FilterCustomers(ctx context.Context, payload map[string]any) (*response.BaseResponse, error) {
	res, err := c.customerClient.FilterCustomers(ctx, payload)
	if err != nil {
		log.Error(ctx, "CustomerService: FilterCustomers error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CustomerService) DeleteCustomer(ctx context.Context, customerId int64) (*response.BaseResponse, error) {
	res, err := c.customerClient.DeleteCustomer(ctx, customerId)
	if err != nil {
		log.Error(ctx, "CustomerService: DeleteCustomer error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewCustomerService(customerClient port.ICustomerClientPort) ICustomerService {
	return &CustomerService{customerClient: customerClient}
}
