/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/purchase"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IShipmentClientPort interface {
	CreateShipment(ctx context.Context, req *request.CreateShipmentRequest) (*response.BaseResponse, error)
	UpdateShipment(ctx context.Context, shipmentId string, req *request.UpdateShipmentRequest) (*response.BaseResponse, error)
	DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipmentsByOrderId(ctx context.Context, orderId string) (*response.BaseResponse, error)
	ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)

	// Item management
	AddItemToShipment(ctx context.Context, shipmentId string, req *request.AddItemToShipmentRequest) (*response.BaseResponse, error)
	UpdateItemInShipment(ctx context.Context, shipmentId string, itemId string, req *request.UpdateItemInShipmentRequest) (*response.BaseResponse, error)
	DeleteItemFromShipment(ctx context.Context, shipmentId string, itemId string) (*response.BaseResponse, error)

	// Facility management
	UpdateShipmentFacility(ctx context.Context, shipmentId string, req *request.UpdateShipmentFacilityRequest) (*response.BaseResponse, error)
}
