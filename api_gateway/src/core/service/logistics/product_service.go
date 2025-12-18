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

type IProductService interface {
	CreateProduct(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateProduct(ctx context.Context, productId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProducts(ctx context.Context, page, size int, sortBy, sortDirection, query, categoryId, statusId string) (*response.BaseResponse, error)
}

type ProductService struct {
	productClient port.IProductClientPort
}

func (p *ProductService) CreateProduct(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := p.productClient.CreateProduct(ctx, req)
	if err != nil {
		log.Error(ctx, "ProductService: CreateProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) UpdateProduct(ctx context.Context, productId string, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := p.productClient.UpdateProduct(ctx, productId, req)
	if err != nil {
		log.Error(ctx, "ProductService: UpdateProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error) {
	res, err := p.productClient.DeleteProduct(ctx, productId)
	if err != nil {
		log.Error(ctx, "ProductService: DeleteProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error) {
	res, err := p.productClient.GetProduct(ctx, productId)
	if err != nil {
		log.Error(ctx, "ProductService: GetProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) GetProducts(ctx context.Context, page, size int, sortBy, sortDirection, query, categoryId, statusId string) (*response.BaseResponse, error) {
	res, err := p.productClient.GetProducts(ctx, page, size, sortBy, sortDirection, query, categoryId, statusId)
	if err != nil {
		log.Error(ctx, "ProductService: GetProducts error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewProductService(productClient port.IProductClientPort) IProductService {
	return &ProductService{
		productClient: productClient,
	}
}
