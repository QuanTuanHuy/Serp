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

type IAddressClientPort interface {
	CreateAddress(ctx context.Context, req *request.CreateAddressRequest) (*response.BaseResponse, error)
	UpdateAddress(ctx context.Context, addressId string, req *request.UpdateAddressRequest) (*response.BaseResponse, error)
	GetAddressesByEntityId(ctx context.Context, entityId string) (*response.BaseResponse, error)
}
