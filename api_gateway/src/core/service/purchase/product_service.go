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

type IProductService interface {
	CreateProduct(ctx context.Context, req *request.CreateProductRequest) (*response.BaseResponse, error)
	UpdateProduct(ctx context.Context, productId string, req *request.UpdateProductRequest) (*response.BaseResponse, error)
	DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProducts(ctx context.Context, params *request.GetProductParams) (*response.BaseResponse, error)
}

type ProductService struct {
	productClient port.IProductClientPort
}

func (p *ProductService) CreateProduct(ctx context.Context, req *request.CreateProductRequest) (*response.BaseResponse, error) {
	res, err := p.productClient.CreateProduct(ctx, req)
	if err != nil {
		log.Error(ctx, "ProductService: CreateProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) UpdateProduct(ctx context.Context, productId string, req *request.UpdateProductRequest) (*response.BaseResponse, error) {
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

func (p *ProductService) GetProducts(ctx context.Context, params *request.GetProductParams) (*response.BaseResponse, error) {
	res, err := p.productClient.GetProducts(ctx, params)
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
