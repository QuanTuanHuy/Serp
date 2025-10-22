/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package port

import (
	"context"

	request "github.com/serp/api-gateway/src/core/domain/dto/request/account"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IUserClientPort interface {
	GetMyProfile(ctx context.Context) (*response.BaseResponse, error)
	GetUsers(ctx context.Context, params *request.GetUserParams) (*response.BaseResponse, error)
	AssignRolesToUser(ctx context.Context, req *request.AssignRoleToUserDto) (*response.BaseResponse, error)
}
