/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

import "github.com/serp/ptm-task/src/core/domain/entity"

type ProjectMatchDTO struct {
	Project  *entity.ProjectEntity
	Distance int
}
