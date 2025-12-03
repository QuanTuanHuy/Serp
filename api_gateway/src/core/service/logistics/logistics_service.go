/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package service

import (
	"context"

	"github.com/golibs-starter/golib/log"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/logistics"
	"github.com/serp/api-gateway/src/core/domain/dto/response"
	port "github.com/serp/api-gateway/src/core/port/client/logistics"
)

type IAddressService interface {
	CreateAddress(ctx context.Context, req *request.AddressCreationForm) (*response.BaseResponse, error)
	UpdateAddress(ctx context.Context, addressId string, req *request.AddressUpdateForm) (*response.BaseResponse, error)
	GetAddressesByEntityId(ctx context.Context, entityId string) (*response.BaseResponse, error)
}

type AddressService struct {
	addressClient port.IAddressClientPort
}

func (a *AddressService) CreateAddress(ctx context.Context, req *request.AddressCreationForm) (*response.BaseResponse, error) {
	res, err := a.addressClient.CreateAddress(ctx, req)
	if err != nil {
		log.Error(ctx, "AddressService: CreateAddress error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (a *AddressService) UpdateAddress(ctx context.Context, addressId string, req *request.AddressUpdateForm) (*response.BaseResponse, error) {
	res, err := a.addressClient.UpdateAddress(ctx, addressId, req)
	if err != nil {
		log.Error(ctx, "AddressService: UpdateAddress error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (a *AddressService) GetAddressesByEntityId(ctx context.Context, entityId string) (*response.BaseResponse, error) {
	res, err := a.addressClient.GetAddressesByEntityId(ctx, entityId)
	if err != nil {
		log.Error(ctx, "AddressService: GetAddressesByEntityId error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewAddressService(addressClient port.IAddressClientPort) IAddressService {
	return &AddressService{
		addressClient: addressClient,
	}
}

type ICategoryService interface {
	CreateCategory(ctx context.Context, req *request.CategoryForm) (*response.BaseResponse, error)
	UpdateCategory(ctx context.Context, categoryId string, req *request.CategoryForm) (*response.BaseResponse, error)
	GetCategories(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
	GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
	DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error)
}

type CategoryService struct {
	categoryClient port.ICategoryClientPort
}

func (c *CategoryService) CreateCategory(ctx context.Context, req *request.CategoryForm) (*response.BaseResponse, error) {
	res, err := c.categoryClient.CreateCategory(ctx, req)
	if err != nil {
		log.Error(ctx, "CategoryService: CreateCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) UpdateCategory(ctx context.Context, categoryId string, req *request.CategoryForm) (*response.BaseResponse, error) {
	res, err := c.categoryClient.UpdateCategory(ctx, categoryId, req)
	if err != nil {
		log.Error(ctx, "CategoryService: UpdateCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) GetCategories(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
	res, err := c.categoryClient.GetCategories(ctx, page, size, sortBy, sortDirection, query, statusId)
	if err != nil {
		log.Error(ctx, "CategoryService: GetCategories error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) GetCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	res, err := c.categoryClient.GetCategory(ctx, categoryId)
	if err != nil {
		log.Error(ctx, "CategoryService: GetCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (c *CategoryService) DeleteCategory(ctx context.Context, categoryId string) (*response.BaseResponse, error) {
	res, err := c.categoryClient.DeleteCategory(ctx, categoryId)
	if err != nil {
		log.Error(ctx, "CategoryService: DeleteCategory error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewCategoryService(categoryClient port.ICategoryClientPort) ICategoryService {
	return &CategoryService{
		categoryClient: categoryClient,
	}
}

type ICustomerService interface {
	GetCustomers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
}

type CustomerService struct {
	customerClient port.ICustomerClientPort
}

func (c *CustomerService) GetCustomers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
	res, err := c.customerClient.GetCustomers(ctx, page, size, sortBy, sortDirection, query, statusId)
	if err != nil {
		log.Error(ctx, "CustomerService: GetCustomers error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewCustomerService(customerClient port.ICustomerClientPort) ICustomerService {
	return &CustomerService{
		customerClient: customerClient,
	}
}

type IFacilityService interface {
	CreateFacility(ctx context.Context, req *request.FacilityCreationForm) (*response.BaseResponse, error)
	UpdateFacility(ctx context.Context, facilityId string, req *request.FacilityUpdateForm) (*response.BaseResponse, error)
	GetFacilities(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
	GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
	DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error)
}

type FacilityService struct {
	facilityClient port.IFacilityClientPort
}

func (f *FacilityService) CreateFacility(ctx context.Context, req *request.FacilityCreationForm) (*response.BaseResponse, error) {
	res, err := f.facilityClient.CreateFacility(ctx, req)
	if err != nil {
		log.Error(ctx, "FacilityService: CreateFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) UpdateFacility(ctx context.Context, facilityId string, req *request.FacilityUpdateForm) (*response.BaseResponse, error) {
	res, err := f.facilityClient.UpdateFacility(ctx, facilityId, req)
	if err != nil {
		log.Error(ctx, "FacilityService: UpdateFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) GetFacilities(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
	res, err := f.facilityClient.GetFacilities(ctx, page, size, sortBy, sortDirection, query, statusId)
	if err != nil {
		log.Error(ctx, "FacilityService: GetFacilities error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) GetFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	res, err := f.facilityClient.GetFacility(ctx, facilityId)
	if err != nil {
		log.Error(ctx, "FacilityService: GetFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (f *FacilityService) DeleteFacility(ctx context.Context, facilityId string) (*response.BaseResponse, error) {
	res, err := f.facilityClient.DeleteFacility(ctx, facilityId)
	if err != nil {
		log.Error(ctx, "FacilityService: DeleteFacility error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewFacilityService(facilityClient port.IFacilityClientPort) IFacilityService {
	return &FacilityService{
		facilityClient: facilityClient,
	}
}

type IInventoryItemService interface {
	CreateInventoryItem(ctx context.Context, req *request.InventoryItemCreationForm) (*response.BaseResponse, error)
	UpdateInventoryItem(ctx context.Context, inventoryItemId string, req *request.InventoryItemUpdateForm) (*response.BaseResponse, error)
	DeleteInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error)
	GetInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error)
	GetInventoryItems(ctx context.Context, page, size int, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId string) (*response.BaseResponse, error)
}

type InventoryItemService struct {
	inventoryItemClient port.IInventoryItemClientPort
}

func (i *InventoryItemService) CreateInventoryItem(ctx context.Context, req *request.InventoryItemCreationForm) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.CreateInventoryItem(ctx, req)
	if err != nil {
		log.Error(ctx, "InventoryItemService: CreateInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) UpdateInventoryItem(ctx context.Context, inventoryItemId string, req *request.InventoryItemUpdateForm) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.UpdateInventoryItem(ctx, inventoryItemId, req)
	if err != nil {
		log.Error(ctx, "InventoryItemService: UpdateInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) DeleteInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.DeleteInventoryItem(ctx, inventoryItemId)
	if err != nil {
		log.Error(ctx, "InventoryItemService: DeleteInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) GetInventoryItem(ctx context.Context, inventoryItemId string) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.GetInventoryItem(ctx, inventoryItemId)
	if err != nil {
		log.Error(ctx, "InventoryItemService: GetInventoryItem error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (i *InventoryItemService) GetInventoryItems(ctx context.Context, page, size int, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId string) (*response.BaseResponse, error) {
	res, err := i.inventoryItemClient.GetInventoryItems(ctx, page, size, sortBy, sortDirection, query, productId, facilityId, expirationDateFrom, expirationDateTo, manufacturingDateFrom, manufacturingDateTo, statusId)
	if err != nil {
		log.Error(ctx, "InventoryItemService: GetInventoryItems error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewInventoryItemService(inventoryItemClient port.IInventoryItemClientPort) IInventoryItemService {
	return &InventoryItemService{
		inventoryItemClient: inventoryItemClient,
	}
}

type IOrderService interface {
	GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error)
	GetOrders(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter string) (*response.BaseResponse, error)
}

type OrderService struct {
	orderClient port.IOrderClientPort
}

func (o *OrderService) GetOrder(ctx context.Context, orderId string) (*response.BaseResponse, error) {
	res, err := o.orderClient.GetOrder(ctx, orderId)
	if err != nil {
		log.Error(ctx, "OrderService: GetOrder error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (o *OrderService) GetOrders(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter string) (*response.BaseResponse, error) {
	res, err := o.orderClient.GetOrders(ctx, page, size, sortBy, sortDirection, query, statusId, orderTypeId, toCustomerId, fromSupplierId, saleChannelId, orderDateAfter, orderDateBefore, deliveryBefore, deliveryAfter)
	if err != nil {
		log.Error(ctx, "OrderService: GetOrders error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewOrderService(orderClient port.IOrderClientPort) IOrderService {
	return &OrderService{
		orderClient: orderClient,
	}
}

type IProductService interface {
	CreateProduct(ctx context.Context, req *request.ProductCreationForm) (*response.BaseResponse, error)
	UpdateProduct(ctx context.Context, productId string, req *request.ProductUpdateForm) (*response.BaseResponse, error)
	DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error)
	GetProducts(ctx context.Context, page, size int, sortBy, sortDirection, query, categoryId, statusId string) (*response.BaseResponse, error)
}

type ProductService struct {
	productClient port.IProductClientPort
}

func (p *ProductService) CreateProduct(ctx context.Context, req *request.ProductCreationForm) (*response.BaseResponse, error) {
	res, err := p.productClient.CreateProduct(ctx, req)
	if err != nil {
		log.Error(ctx, "ProductService: CreateProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) UpdateProduct(ctx context.Context, productId string, req *request.ProductUpdateForm) (*response.BaseResponse, error) {
	res, err := p.productClient.UpdateProduct(ctx, productId, req)
	if err != nil {
		log.Error(ctx, "ProductService: UpdateProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) DeleteProduct(ctx context.Context, productId string) (*response.BaseResponse, error) {
	res, err := p.productClient.DeleteProduct(ctx, productId)
	if err != nil {
		log.Error(ctx, "ProductService: DeleteProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) GetProduct(ctx context.Context, productId string) (*response.BaseResponse, error) {
	res, err := p.productClient.GetProduct(ctx, productId)
	if err != nil {
		log.Error(ctx, "ProductService: GetProduct error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (p *ProductService) GetProducts(ctx context.Context, page, size int, sortBy, sortDirection, query, categoryId, statusId string) (*response.BaseResponse, error) {
	res, err := p.productClient.GetProducts(ctx, page, size, sortBy, sortDirection, query, categoryId, statusId)
	if err != nil {
		log.Error(ctx, "ProductService: GetProducts error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewProductService(productClient port.IProductClientPort) IProductService {
	return &ProductService{
		productClient: productClient,
	}
}

type ISupplierService interface {
	GetSuppliers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error)
	GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error)
}

type SupplierService struct {
	supplierClient port.ISupplierClientPort
}

func (s *SupplierService) GetSuppliers(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId string) (*response.BaseResponse, error) {
	res, err := s.supplierClient.GetSuppliers(ctx, page, size, sortBy, sortDirection, query, statusId)
	if err != nil {
		log.Error(ctx, "SupplierService: GetSuppliers error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *SupplierService) GetSupplier(ctx context.Context, supplierId string) (*response.BaseResponse, error) {
	res, err := s.supplierClient.GetSupplier(ctx, supplierId)
	if err != nil {
		log.Error(ctx, "SupplierService: GetSupplier error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewSupplierService(supplierClient port.ISupplierClientPort) ISupplierService {
	return &SupplierService{
		supplierClient: supplierClient,
	}
}

type IShipmentService interface {
	CreateShipment(ctx context.Context, req *request.ShipmentCreationForm) (*response.BaseResponse, error)
	UpdateShipment(ctx context.Context, shipmentId string, req *request.ShipmentUpdateForm) (*response.BaseResponse, error)
	ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	AddItemToShipment(ctx context.Context, shipmentId string, req *request.InventoryItemDetailForm) (*response.BaseResponse, error)
	UpdateItemInShipment(ctx context.Context, shipmentId, itemId string, req *request.InventoryItemDetailUpdateForm) (*response.BaseResponse, error)
	DeleteItemFromShipment(ctx context.Context, shipmentId, itemId string) (*response.BaseResponse, error)
	GetShipmentDetail(ctx context.Context, shipmentId string) (*response.BaseResponse, error)
	GetShipments(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId string) (*response.BaseResponse, error)
}

type ShipmentService struct {
	shipmentClient port.IShipmentClientPort
}

func (s *ShipmentService) CreateShipment(ctx context.Context, req *request.ShipmentCreationForm) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.CreateShipment(ctx, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: CreateShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) UpdateShipment(ctx context.Context, shipmentId string, req *request.ShipmentUpdateForm) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.UpdateShipment(ctx, shipmentId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: UpdateShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) ImportShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.ImportShipment(ctx, shipmentId)
	if err != nil {
		log.Error(ctx, "ShipmentService: ImportShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) DeleteShipment(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.DeleteShipment(ctx, shipmentId)
	if err != nil {
		log.Error(ctx, "ShipmentService: DeleteShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) AddItemToShipment(ctx context.Context, shipmentId string, req *request.InventoryItemDetailForm) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.AddItemToShipment(ctx, shipmentId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: AddItemToShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) UpdateItemInShipment(ctx context.Context, shipmentId, itemId string, req *request.InventoryItemDetailUpdateForm) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.UpdateItemInShipment(ctx, shipmentId, itemId, req)
	if err != nil {
		log.Error(ctx, "ShipmentService: UpdateItemInShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) DeleteItemFromShipment(ctx context.Context, shipmentId, itemId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.DeleteItemFromShipment(ctx, shipmentId, itemId)
	if err != nil {
		log.Error(ctx, "ShipmentService: DeleteItemFromShipment error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) GetShipmentDetail(ctx context.Context, shipmentId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.GetShipmentDetail(ctx, shipmentId)
	if err != nil {
		log.Error(ctx, "ShipmentService: GetShipmentDetail error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func (s *ShipmentService) GetShipments(ctx context.Context, page, size int, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId string) (*response.BaseResponse, error) {
	res, err := s.shipmentClient.GetShipments(ctx, page, size, sortBy, sortDirection, query, statusId, shipmentTypeId, toCustomerId, fromSupplierId, orderId)
	if err != nil {
		log.Error(ctx, "ShipmentService: GetShipments error: ", err.Error())
		return nil, err
	}
	return res, nil
}

func NewShipmentService(shipmentClient port.IShipmentClientPort) IShipmentService {
	return &ShipmentService{
		shipmentClient: shipmentClient,
	}
}
