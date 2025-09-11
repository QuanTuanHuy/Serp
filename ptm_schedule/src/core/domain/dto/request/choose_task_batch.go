/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type ChooseTaskBatchRequest struct {
	BatchNumber int32 `json:"batchNumber" validate:"min=0"`
}
