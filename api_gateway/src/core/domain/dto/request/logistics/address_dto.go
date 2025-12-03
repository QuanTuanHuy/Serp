/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package request

type AddressCreationForm struct {
	EntityId    string  `json:"entityId"`
	EntityType  string  `json:"entityType"`
	AddressType string  `json:"addressType"`
	Latitude    float32 `json:"latitude"`
	Longitude   float32 `json:"longitude"`
	IsDefault   bool    `json:"isDefault"`
	FullAddress string  `json:"fullAddress"`
}

type AddressUpdateForm struct {
	AddressType string  `json:"addressType"`
	Latitude    float32 `json:"latitude"`
	Longitude   float32 `json:"longitude"`
	IsDefault   bool    `json:"isDefault"`
	FullAddress string  `json:"fullAddress"`
}

type CategoryForm struct {
	Name string `json:"name"`
}

type FacilityCreationForm struct {
	Name             string  `json:"name"`
	Phone            string  `json:"phone"`
	StatusId         string  `json:"statusId"`
	PostalCode       string  `json:"postalCode"`
	Length           float32 `json:"length"`
	Width            float32 `json:"width"`
	Height           float32 `json:"height"`
	AddressType      string  `json:"addressType"`
	Latitude         float32 `json:"latitude"`
	Longitude        float32 `json:"longitude"`
	IsAddressDefault bool    `json:"isAddressDefault"`
	FullAddress      string  `json:"fullAddress"`
}

type FacilityUpdateForm struct {
	Name       string  `json:"name"`
	IsDefault  bool    `json:"isDefault"`
	StatusId   string  `json:"statusId"`
	Phone      string  `json:"phone"`
	PostalCode string  `json:"postalCode"`
	Length     float32 `json:"length"`
	Width      float32 `json:"width"`
	Height     float32 `json:"height"`
}

type InventoryItemCreationForm struct {
	ProductId         string `json:"productId"`
	Quantity          int    `json:"quantity"`
	FacilityId        string `json:"facilityId"`
	ExpirationDate    string `json:"expirationDate"`    // LocalDate format: "2024-12-31"
	ManufacturingDate string `json:"manufacturingDate"` // LocalDate format: "2024-12-31"
	StatusId          string `json:"statusId"`
}

type InventoryItemUpdateForm struct {
	Quantity          int    `json:"quantity"`
	ExpirationDate    string `json:"expirationDate"`    // LocalDate format: "2024-12-31"
	ManufacturingDate string `json:"manufacturingDate"` // LocalDate format: "2024-12-31"
	StatusId          string `json:"statusId"`
}

type ProductCreationForm struct {
	Name           string  `json:"name"`
	Weight         float64 `json:"weight"`
	Height         float64 `json:"height"`
	Unit           string  `json:"unit"`
	CostPrice      int64   `json:"costPrice"`
	WholeSalePrice int64   `json:"wholeSalePrice"`
	RetailPrice    int64   `json:"retailPrice"`
	CategoryId     string  `json:"categoryId"`
	StatusId       string  `json:"statusId"`
	ImageId        string  `json:"imageId"`
	ExtraProps     string  `json:"extraProps"`
	VatRate        float32 `json:"vatRate"`
	SkuCode        string  `json:"skuCode"`
}

type ProductUpdateForm struct {
	Name           string  `json:"name"`
	Weight         float64 `json:"weight"`
	Height         float64 `json:"height"`
	Unit           string  `json:"unit"`
	CostPrice      int64   `json:"costPrice"`
	WholeSalePrice int64   `json:"wholeSalePrice"`
	RetailPrice    int64   `json:"retailPrice"`
	StatusId       string  `json:"statusId"`
	ImageId        string  `json:"imageId"`
	ExtraProps     string  `json:"extraProps"`
	VatRate        float32 `json:"vatRate"`
	SkuCode        string  `json:"skuCode"`
}

type ShipmentCreationForm struct {
	ShipmentName         string                    `json:"shipmentName"`
	ShipmentTypeId       string                    `json:"shipmentTypeId"`
	StatusId             string                    `json:"statusId"`
	FromSupplierId       string                    `json:"fromSupplierId"`
	ToCustomerId         string                    `json:"toCustomerId"`
	OrderId              string                    `json:"orderId"`
	Note                 string                    `json:"note"`
	ExpectedDeliveryDate string                    `json:"expectedDeliveryDate"`
	Items                []InventoryItemDetailForm `json:"items"`
}

type InventoryItemDetailForm struct {
	ProductId         string `json:"productId"`
	Quantity          int    `json:"quantity"`
	OrderItemId       string `json:"orderItemId"`
	Note              string `json:"note"`
	LotId             string `json:"lotId"`
	FacilityId        string `json:"facilityId"`
	ExpirationDate    string `json:"expirationDate"`
	ManufacturingDate string `json:"manufacturingDate"`
	StatusId          string `json:"statusId"`
}

type ShipmentUpdateForm struct {
	ShipmentName   string `json:"shipmentName"`
	ShipmentTypeId string `json:"shipmentTypeId"`
	StatusId       string `json:"statusId"`
	FromSupplierId string `json:"fromSupplierId"`
	ToCustomerId   string `json:"toCustomerId"`
	OrderId        string `json:"orderId"`
}

type InventoryItemDetailUpdateForm struct {
	Quantity          int    `json:"quantity"`
	ExpirationDate    string `json:"expirationDate"`
	ManufacturingDate string `json:"manufacturingDate"`
	StatusId          string `json:"statusId"`
}
