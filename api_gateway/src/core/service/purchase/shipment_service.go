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

type IShipmentService interface {
	CreateShipment(ctx context.Context, req *request.CreateShipmentRequest) (*response.BaseResponse, error)
	UpdateShipment(ctx context.Context, shipmentId string, req *request.UpdateShipmentRequest) (*response.BaseResponse, error)
	DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipmentsByOrderId(ctx context.Context, orderId string) (*response.BaseResponse, error)
	ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)

	AddItemToShipment(ctx context.Context, shipmentId string, req *request.AddItemToShipmentRequest) (*response.BaseResponse, error)
	UpdateItemInShipment(ctx context.Context, shipmentId string, itemId string, req *request.UpdateItemInShipmentRequest) (*response.BaseResponse, error)
	DeleteItemFromShipment(ctx context.Context, shipmentId string, itemId string) (*response.BaseResponse, error)

	UpdateShipmentFacility(ctx context.Context, shipmentId string, req *request.UpdateShipmentFacilityRequest) (*response.BaseResponse, error)
}

type ShipmentService struct {
	shipmentClient port.IShipmentClientPort
}

func (s *ShipmentService) CreateShipment(ctx context.Context, req *request.CreateShipmentRequest) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.CreateShipment(ctx, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: CreateShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) UpdateShipment(ctx context.Context, shipmentId string, req *request.UpdateShipmentRequest) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.UpdateShipment(ctx, shipmentId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: UpdateShipment error: ", err.Error())
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

func (s *ShipmentService) GetShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.GetShipment(ctx, shipmentId)
	if err != nil {
		log.Error(ctx, "ShipmentService: GetShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) GetShipmentsByOrderId(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.GetShipmentsByOrderId(ctx, orderId)
	if err != nil {
		log.Error(ctx, "ShipmentService: GetShipmentsByOrderId error: ", err.Error())
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

func (s *ShipmentService) AddItemToShipment(ctx context.Context, shipmentId string, req *request.AddItemToShipmentRequest) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.AddItemToShipment(ctx, shipmentId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: AddItemToShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) UpdateItemInShipment(ctx context.Context, shipmentId string, itemId string, req *request.UpdateItemInShipmentRequest) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.UpdateItemInShipment(ctx, shipmentId, itemId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: UpdateItemInShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) DeleteItemFromShipment(ctx context.Context, shipmentId string, itemId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.DeleteItemFromShipment(ctx, shipmentId, itemId)
	if err != nil {
		log.Error(ctx, "ShipmentService: DeleteItemFromShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) UpdateShipmentFacility(ctx context.Context, shipmentId string, req *request.UpdateShipmentFacilityRequest) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.UpdateShipmentFacility(ctx, shipmentId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: UpdateShipmentFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewShipmentService(shipmentClient port.IShipmentClientPort) IShipmentService {
	return &ShipmentService{
		shipmentClient: shipmentClient,
	}
}
