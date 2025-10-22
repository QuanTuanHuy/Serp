/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package port

import (
	"context"

	"github.com/serp/api-gateway/src/core/domain/dto/response"
)

type IOrganizationClientPort interface {
	GetOrganizations(ctx context.Context, search *string, status *string, organizationType *string, page *int, pageSize *int) (*response.BaseResponse, error)
	GetOrganizationById(ctx context.Context, organizationID int64) (*response.BaseResponse, error)
	GetMyOrganization(ctx context.Context) (*response.BaseResponse, error)
}
