/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IAddressClientPort interface {
	CreateAddress(ctx context.Context, req map[string]interface{}) (*response.BaseResponse, error)
	UpdateAddress(ctx context.Context, addressId string, req map[string]interface{}) (*response.BaseResponse, error)
	GetAddressesByEntityId(ctx context.Context, entityId string) (*response.BaseResponse, error)
}
