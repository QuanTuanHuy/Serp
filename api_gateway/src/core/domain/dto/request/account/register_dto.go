/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type RegisterDTO struct {
	FirstName    string                `json:"firstName"`
	LastName     string                `json:"lastName"`
	Email        string                `json:"email"`
	Password     string                `json:"password"`
	Organization CreateOrganizationDTO `json:"organization"`
}

type CreateOrganizationDTO struct {
	Name string `json:"name"`
}
