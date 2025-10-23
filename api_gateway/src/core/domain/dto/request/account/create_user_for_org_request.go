/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateUserForOrgRequest struct {
	FirstName string   `json:"firstName"`
	LastName  string   `json:"lastName"`
	Email     string   `json:"email"`
	Password  string   `json:"password"`
	UserType  *string  `json:"userType"`
	RoleIds   []*int64 `json:"roleIds"`
}
