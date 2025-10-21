/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreatePermissionDto struct {
	Name        string  `json:"name"`
	Description *string `json:"description"`
	Resource    string  `json:"resource"`
	Action      string  `json:"action"`
}
