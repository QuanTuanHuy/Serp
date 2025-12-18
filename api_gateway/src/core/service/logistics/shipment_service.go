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

type IShipmentService interface {
	CreateShipment(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error)
	ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	AddItemToShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateItemInShipment(ctx context.Context, shipmentId, itemId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteItemFromShipment(ctx context.Context, shipmentId, itemId string) (*response.BaseResponse, error)
	GetShipmentDetail(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipments(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId string) (*response.BaseResponse, error)
}

type ShipmentService struct {
	shipmentClient port.IShipmentClientPort
}

func (s *ShipmentService) CreateShipment(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.CreateShipment(ctx, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: CreateShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) UpdateShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.UpdateShipment(ctx, shipmentId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: UpdateShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.ImportShipment(ctx, shipmentId)
	if err != nil {
		log.Error(ctx, "ShipmentService: ImportShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.DeleteShipment(ctx, shipmentId)
	if err != nil {
		log.Error(ctx, "ShipmentService: DeleteShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) AddItemToShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.AddItemToShipment(ctx, shipmentId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: AddItemToShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) UpdateItemInShipment(ctx context.Context, shipmentId, itemId string, req map[string]interface{}) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.UpdateItemInShipment(ctx, shipmentId, itemId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: UpdateItemInShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) DeleteItemFromShipment(ctx context.Context, shipmentId, itemId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.DeleteItemFromShipment(ctx, shipmentId, itemId)
	if err != nil {
		log.Error(ctx, "ShipmentService: DeleteItemFromShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) GetShipmentDetail(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.GetShipmentDetail(ctx, shipmentId)
	if err != nil {
		log.Error(ctx, "ShipmentService: GetShipmentDetail error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) GetShipments(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.GetShipments(ctx, page, size, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId)
	if err != nil {
		log.Error(ctx, "ShipmentService: GetShipments error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewShipmentService(shipmentClient port.IShipmentClientPort) IShipmentService {
	return &ShipmentService{
		shipmentClient: shipmentClient,
	}
}
