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

type ICategoryService interface {
	CreateCategory(ctx context.Context, req *request.CreateCategoryRequest) (*response.BaseResponse, error)
	UpdateCategory(ctx context.Context, categoryId string, req *request.UpdateCategoryRequest) (*response.BaseResponse, error)
	DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
	GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
	GetCategories(ctx context.Context, params *request.GetCategoryParams) (*response.BaseResponse, error)
}

type CategoryService struct {
	categoryClient port.ICategoryClientPort
}

func (c *CategoryService) CreateCategory(ctx context.Context, req *request.CreateCategoryRequest) (*response.BaseResponse, error) {
	res, err := c.categoryClient.CreateCategory(ctx, req)
	if err != nil {
		log.Error(ctx, "CategoryService: CreateCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) UpdateCategory(ctx context.Context, categoryId string, req *request.UpdateCategoryRequest) (*response.BaseResponse, error) {
	res, err := c.categoryClient.UpdateCategory(ctx, categoryId, req)
	if err != nil {
		log.Error(ctx, "CategoryService: UpdateCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	res, err := c.categoryClient.DeleteCategory(ctx, categoryId)
	if err != nil {
		log.Error(ctx, "CategoryService: DeleteCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	res, err := c.categoryClient.GetCategory(ctx, categoryId)
	if err != nil {
		log.Error(ctx, "CategoryService: GetCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) GetCategories(ctx context.Context, params *request.GetCategoryParams) (*response.BaseResponse, error) {
	res, err := c.categoryClient.GetCategories(ctx, params)
	if err != nil {
		log.Error(ctx, "CategoryService: GetCategories error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewCategoryService(categoryClient port.ICategoryClientPort) ICategoryService {
	return &CategoryService{
		categoryClient: categoryClient,
	}
}
