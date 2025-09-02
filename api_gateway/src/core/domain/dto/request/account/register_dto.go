/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type RegisterDTO struct {
	FullName string `json:"fullName"`
	Email    string `json:"email"`
	Password string `json:"password"`
}
