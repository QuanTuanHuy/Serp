/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IShipmentClientPort interface {
	CreateShipment(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipmentsByOrderId(ctx context.Context, orderId string) (*response.BaseResponse, error)
	ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)

	// Item management
	AddItemToShipment(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateItemInShipment(ctx context.Context, shipmentId string, itemId string, req map[string]interface{}) (*response.BaseResponse, error)
	DeleteItemFromShipment(ctx context.Context, shipmentId string, itemId string) (*response.BaseResponse, error)

	// Facility management
	UpdateShipmentFacility(ctx context.Context, shipmentId string, req map[string]interface{}) (*response.BaseResponse, error)
}
