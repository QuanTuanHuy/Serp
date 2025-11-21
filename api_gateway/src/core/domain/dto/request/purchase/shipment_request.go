/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type InventoryItemDetail struct {
	ProductId         string `json:"productId"`
	Quantity          int    `json:"quantity"`
	OrderItemId       string `json:"orderItemId"`
	Note              string `json:"note"`
	LotId             string `json:"lotId"`
	ExpirationDate    string `json:"expirationDate"`    // LocalDate as string "2025-11-19"
	ManufacturingDate string `json:"manufacturingDate"` // LocalDate as string "2025-11-19"
	FacilityId        string `json:"facilityId"`
}

type CreateShipmentRequest struct {
	FromSupplierId       string                `json:"fromSupplierId"`
	OrderId              string                `json:"orderId"`
	ShipmentName         string                `json:"shipmentName"`
	Note                 string                `json:"note"`
	ExpectedDeliveryDate string                `json:"expectedDeliveryDate"` // LocalDate as string
	Items                []InventoryItemDetail `json:"items"`
}

type UpdateShipmentRequest struct {
	ShipmentName         string `json:"shipmentName"`
	Note                 string `json:"note"`
	ExpectedDeliveryDate string `json:"expectedDeliveryDate"`
}

type AddItemToShipmentRequest struct {
	ProductId         string `json:"productId"`
	Quantity          int    `json:"quantity"`
	OrderItemId       string `json:"orderItemId"`
	Note              string `json:"note"`
	LotId             string `json:"lotId"`
	ExpirationDate    string `json:"expirationDate"`
	ManufacturingDate string `json:"manufacturingDate"`
	FacilityId        string `json:"facilityId"`
}

type UpdateItemInShipmentRequest struct {
	Quantity          int    `json:"quantity"`
	Note              string `json:"note"`
	LotId             string `json:"lotId"`
	ExpirationDate    string `json:"expirationDate"`
	ManufacturingDate string `json:"manufacturingDate"`
}

type UpdateShipmentFacilityRequest struct {
	FacilityId string `json:"facilityId"`
}
