/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package response

type BaseResponse struct {
	Code    int    `json:"code"`
	Status  string `json:"status"`
	Message string `json:"message"`
	Data    any    `json:"data"`
}
