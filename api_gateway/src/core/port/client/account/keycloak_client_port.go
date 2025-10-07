/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IKeycloakClientPort interface {
	GetKeycloakClientSecret(ctx context.Context, clientId string) (*response.BaseResponse, error)
}
