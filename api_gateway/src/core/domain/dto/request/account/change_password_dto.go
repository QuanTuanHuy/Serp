/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type ChangePasswordDTO struct {
	OldPassword string `json:"oldPassword"`
	NewPassword string `json:"newPassword"`
}
