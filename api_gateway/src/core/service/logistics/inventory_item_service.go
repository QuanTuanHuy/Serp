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

type IInventoryItemService interface {
	CreateInventoryItem(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateInventoryItem(ctx context.Context, inventoryItemId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error)
	GetInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error)
	GetInventoryItems(ctx context.Context, page, size int, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId string) (*response.BaseResponse, error)
}

type InventoryItemService struct {
	inventoryItemClient port.IInventoryItemClientPort
}

func (i *InventoryItemService) CreateInventoryItem(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.CreateInventoryItem(ctx, req)
	if err != nil {
		log.Error(ctx, "InventoryItemService: CreateInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) UpdateInventoryItem(ctx context.Context, inventoryItemId string, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.UpdateInventoryItem(ctx, inventoryItemId, req)
	if err != nil {
		log.Error(ctx, "InventoryItemService: UpdateInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) DeleteInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.DeleteInventoryItem(ctx, inventoryItemId)
	if err != nil {
		log.Error(ctx, "InventoryItemService: DeleteInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) GetInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.GetInventoryItem(ctx, inventoryItemId)
	if err != nil {
		log.Error(ctx, "InventoryItemService: GetInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) GetInventoryItems(ctx context.Context, page, size int, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId string) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.GetInventoryItems(ctx, page, size, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId)
	if err != nil {
		log.Error(ctx, "InventoryItemService: GetInventoryItems error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewInventoryItemService(inventoryItemClient port.IInventoryItemClientPort) IInventoryItemService {
	return &InventoryItemService{
		inventoryItemClient: inventoryItemClient,
	}
}
