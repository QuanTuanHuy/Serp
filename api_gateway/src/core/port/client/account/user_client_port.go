/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IUserClientPort interface {
	GetMyProfile(ctx context.Context) (*response.BaseResponse, error)
}
