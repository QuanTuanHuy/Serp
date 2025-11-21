/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type CreateAddressRequest struct {
	Name       string `json:"name"`
	Street     string `json:"street"`
	City       string `json:"city"`
	State      string `json:"state"`
	Country    string `json:"country"`
	PostalCode string `json:"postalCode"`
	IsDefault  *bool  `json:"isDefault"`
	ExtraProps string `json:"extraProps"`
}

type UpdateAddressRequest struct {
	Name       string `json:"name"`
	Street     string `json:"street"`
	City       string `json:"city"`
	State      string `json:"state"`
	Country    string `json:"country"`
	PostalCode string `json:"postalCode"`
	IsDefault  *bool  `json:"isDefault"`
	ExtraProps string `json:"extraProps"`
}
