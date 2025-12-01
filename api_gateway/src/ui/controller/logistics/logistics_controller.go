/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/core/domain/constant"
	request "github.com/serp/api-gateway/src/core/domain/dto/request/logistics"
	service "github.com/serp/api-gateway/src/core/service/logistics"
	"github.com/serp/api-gateway/src/kernel/utils"
)

type AddressController struct {
	addressService service.IAddressService
}

func (a *AddressController) CreateAddress(c *gin.Context) {
	var req request.AddressCreationForm
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.CreateAddress(c.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}
	c.JSON(res.Code, res)
}

func (a *AddressController) UpdateAddress(c *gin.Context) {
	addressId := c.Param("addressId")
	if addressId == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	var req request.AddressUpdateForm
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.UpdateAddress(c.Request.Context(), addressId, &req)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}
	c.JSON(res.Code, res)
}

func (a *AddressController) GetAddressesByEntityId(c *gin.Context) {
	entityId := c.Param("entityId")
	if entityId == "" {
		utils.AbortErrorHandle(c, constant.GeneralBadRequest)
		return
	}

	res, err := a.addressService.GetAddressesByEntityId(c.Request.Context(), entityId)
	if err != nil {
		utils.AbortErrorHandle(c, constant.GeneralUnauthorized)
		return
	}
	c.JSON(res.Code, res)
}

func NewAddressController(addressService service.IAddressService) *AddressController {
	return &AddressController{
		addressService: addressService,
	}
}

type CategoryController struct {
	categoryService service.ICategoryService
}

func (c *CategoryController) CreateCategory(ctx *gin.Context) {
	var req request.CategoryForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.CreateCategory(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) UpdateCategory(ctx *gin.Context) {
	categoryId := ctx.Param("categoryId")
	if categoryId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.CategoryForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.UpdateCategory(ctx.Request.Context(), categoryId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) GetCategories(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryStr := ""
	if query != nil {
		queryStr = *query
	}
	statusIdStr := ""
	if statusId != nil {
		statusIdStr = *statusId
	}

	res, err := c.categoryService.GetCategories(ctx.Request.Context(), page, pageSize,
		sortByStr, sortDirStr, queryStr, statusIdStr)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) GetCategory(ctx *gin.Context) {
	categoryId := ctx.Param("categoryId")
	if categoryId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.GetCategory(ctx.Request.Context(), categoryId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (c *CategoryController) DeleteCategory(ctx *gin.Context) {
	categoryId := ctx.Param("categoryId")
	if categoryId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := c.categoryService.DeleteCategory(ctx.Request.Context(), categoryId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewCategoryController(categoryService service.ICategoryService) *CategoryController {
	return &CategoryController{
		categoryService: categoryService,
	}
}

type CustomerController struct {
	customerService service.ICustomerService
}

func (c *CustomerController) GetCustomers(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryStr := ""
	if query != nil {
		queryStr = *query
	}
	statusIdStr := ""
	if statusId != nil {
		statusIdStr = *statusId
	}

	res, err := c.customerService.GetCustomers(ctx.Request.Context(), page, pageSize,
		sortByStr, sortDirStr, queryStr, statusIdStr)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewCustomerController(customerService service.ICustomerService) *CustomerController {
	return &CustomerController{
		customerService: customerService,
	}
}

type FacilityController struct {
	facilityService service.IFacilityService
}

func (f *FacilityController) CreateFacility(ctx *gin.Context) {
	var req request.FacilityCreationForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.CreateFacility(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) UpdateFacility(ctx *gin.Context) {
	facilityId := ctx.Param("facilityId")
	if facilityId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.FacilityUpdateForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.UpdateFacility(ctx.Request.Context(), facilityId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) GetFacilities(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryStr := ""
	if query != nil {
		queryStr = *query
	}
	statusIdStr := ""
	if statusId != nil {
		statusIdStr = *statusId
	}

	res, err := f.facilityService.GetFacilities(ctx.Request.Context(), page, pageSize,
		sortByStr, sortDirStr, queryStr, statusIdStr)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) GetFacility(ctx *gin.Context) {
	facilityId := ctx.Param("facilityId")
	if facilityId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.GetFacility(ctx.Request.Context(), facilityId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (f *FacilityController) DeleteFacility(ctx *gin.Context) {
	facilityId := ctx.Param("facilityId")
	if facilityId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := f.facilityService.DeleteFacility(ctx.Request.Context(), facilityId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewFacilityController(facilityService service.IFacilityService) *FacilityController {
	return &FacilityController{
		facilityService: facilityService,
	}
}

type InventoryItemController struct {
	inventoryItemService service.IInventoryItemService
}

func (i *InventoryItemController) CreateInventoryItem(ctx *gin.Context) {
	var req request.InventoryItemCreationForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := i.inventoryItemService.CreateInventoryItem(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (i *InventoryItemController) UpdateInventoryItem(ctx *gin.Context) {
	inventoryItemId := ctx.Param("inventoryItemId")
	if inventoryItemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.InventoryItemUpdateForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := i.inventoryItemService.UpdateInventoryItem(ctx.Request.Context(), inventoryItemId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (i *InventoryItemController) DeleteInventoryItem(ctx *gin.Context) {
	inventoryItemId := ctx.Param("inventoryItemId")
	if inventoryItemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := i.inventoryItemService.DeleteInventoryItem(ctx.Request.Context(), inventoryItemId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (i *InventoryItemController) GetInventoryItem(ctx *gin.Context) {
	inventoryItemId := ctx.Param("inventoryItemId")
	if inventoryItemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := i.inventoryItemService.GetInventoryItem(ctx.Request.Context(), inventoryItemId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (i *InventoryItemController) GetInventoryItems(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	productId := utils.ParseStringQuery(ctx, "productId")
	facilityId := utils.ParseStringQuery(ctx, "facilityId")
	expirationDateFrom := utils.ParseStringQuery(ctx, "expirationDateFrom")
	expirationDateTo := utils.ParseStringQuery(ctx, "expirationDateTo")
	manufacturingDateFrom := utils.ParseStringQuery(ctx, "manufacturingDateFrom")
	manufacturingDateTo := utils.ParseStringQuery(ctx, "manufacturingDateTo")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryVal := ""
	if query != nil {
		queryVal = *query
	}
	productIdVal := ""
	if productId != nil {
		productIdVal = *productId
	}
	facilityIdVal := ""
	if facilityId != nil {
		facilityIdVal = *facilityId
	}
	expirationDateFromVal := ""
	if expirationDateFrom != nil {
		expirationDateFromVal = *expirationDateFrom
	}
	expirationDateToVal := ""
	if expirationDateTo != nil {
		expirationDateToVal = *expirationDateTo
	}
	manufacturingDateFromVal := ""
	if manufacturingDateFrom != nil {
		manufacturingDateFromVal = *manufacturingDateFrom
	}
	manufacturingDateToVal := ""
	if manufacturingDateTo != nil {
		manufacturingDateToVal = *manufacturingDateTo
	}
	statusIdVal := ""
	if statusId != nil {
		statusIdVal = *statusId
	}

	res, err := i.inventoryItemService.GetInventoryItems(
		ctx.Request.Context(),
		page, pageSize, sortByStr, sortDirStr,
		queryVal, productIdVal, facilityIdVal,
		expirationDateFromVal, expirationDateToVal,
		manufacturingDateFromVal, manufacturingDateToVal,
		statusIdVal,
	)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewInventoryItemController(inventoryItemService service.IInventoryItemService) *InventoryItemController {
	return &InventoryItemController{
		inventoryItemService: inventoryItemService,
	}
}

type OrderController struct {
	orderService service.IOrderService
}

func (o *OrderController) GetOrder(ctx *gin.Context) {
	orderId := ctx.Param("orderId")
	if orderId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := o.orderService.GetOrder(ctx.Request.Context(), orderId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (o *OrderController) GetOrders(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")
	orderTypeId := utils.ParseStringQuery(ctx, "orderTypeId")
	toCustomerId := utils.ParseStringQuery(ctx, "toCustomerId")
	fromSupplierId := utils.ParseStringQuery(ctx, "fromSupplierId")
	saleChannelId := utils.ParseStringQuery(ctx, "saleChannelId")
	orderDateAfter := utils.ParseStringQuery(ctx, "orderDateAfter")
	orderDateBefore := utils.ParseStringQuery(ctx, "orderDateBefore")
	deliveryBefore := utils.ParseStringQuery(ctx, "deliveryBefore")
	deliveryAfter := utils.ParseStringQuery(ctx, "deliveryAfter")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryVal := ""
	if query != nil {
		queryVal = *query
	}
	statusIdVal := ""
	if statusId != nil {
		statusIdVal = *statusId
	}
	orderTypeIdVal := ""
	if orderTypeId != nil {
		orderTypeIdVal = *orderTypeId
	}
	toCustomerIdVal := ""
	if toCustomerId != nil {
		toCustomerIdVal = *toCustomerId
	}
	fromSupplierIdVal := ""
	if fromSupplierId != nil {
		fromSupplierIdVal = *fromSupplierId
	}
	saleChannelIdVal := ""
	if saleChannelId != nil {
		saleChannelIdVal = *saleChannelId
	}
	orderDateAfterVal := ""
	if orderDateAfter != nil {
		orderDateAfterVal = *orderDateAfter
	}
	orderDateBeforeVal := ""
	if orderDateBefore != nil {
		orderDateBeforeVal = *orderDateBefore
	}
	deliveryBeforeVal := ""
	if deliveryBefore != nil {
		deliveryBeforeVal = *deliveryBefore
	}
	deliveryAfterVal := ""
	if deliveryAfter != nil {
		deliveryAfterVal = *deliveryAfter
	}

	res, err := o.orderService.GetOrders(
		ctx.Request.Context(),
		page, pageSize, sortByStr, sortDirStr,
		queryVal, statusIdVal, orderTypeIdVal, toCustomerIdVal,
		fromSupplierIdVal, saleChannelIdVal,
		orderDateAfterVal, orderDateBeforeVal,
		deliveryBeforeVal, deliveryAfterVal,
	)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewOrderController(orderService service.IOrderService) *OrderController {
	return &OrderController{
		orderService: orderService,
	}
}

type ProductController struct {
	productService service.IProductService
}

func (p *ProductController) CreateProduct(ctx *gin.Context) {
	var req request.ProductCreationForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.CreateProduct(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (p *ProductController) UpdateProduct(ctx *gin.Context) {
	productId := ctx.Param("productId")
	if productId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.ProductUpdateForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.UpdateProduct(ctx.Request.Context(), productId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (p *ProductController) DeleteProduct(ctx *gin.Context) {
	productId := ctx.Param("productId")
	if productId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.DeleteProduct(ctx.Request.Context(), productId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (p *ProductController) GetProduct(ctx *gin.Context) {
	productId := ctx.Param("productId")
	if productId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := p.productService.GetProduct(ctx.Request.Context(), productId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (p *ProductController) GetProducts(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	categoryId := utils.ParseStringQuery(ctx, "categoryId")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryVal := ""
	if query != nil {
		queryVal = *query
	}
	categoryIdVal := ""
	if categoryId != nil {
		categoryIdVal = *categoryId
	}
	statusIdVal := ""
	if statusId != nil {
		statusIdVal = *statusId
	}

	res, err := p.productService.GetProducts(
		ctx.Request.Context(),
		page, pageSize, sortByStr, sortDirStr,
		queryVal, categoryIdVal, statusIdVal,
	)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewProductController(productService service.IProductService) *ProductController {
	return &ProductController{
		productService: productService,
	}
}

type SupplierController struct {
	supplierService service.ISupplierService
}

func (s *SupplierController) GetSuppliers(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryVal := ""
	if query != nil {
		queryVal = *query
	}
	statusIdVal := ""
	if statusId != nil {
		statusIdVal = *statusId
	}

	res, err := s.supplierService.GetSuppliers(
		ctx.Request.Context(),
		page, pageSize, sortByStr, sortDirStr,
		queryVal, statusIdVal,
	)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *SupplierController) GetSupplier(ctx *gin.Context) {
	supplierId := ctx.Param("supplierId")
	if supplierId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.supplierService.GetSupplier(ctx.Request.Context(), supplierId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewSupplierController(supplierService service.ISupplierService) *SupplierController {
	return &SupplierController{
		supplierService: supplierService,
	}
}

type ShipmentController struct {
	shipmentService service.IShipmentService
}

func (s *ShipmentController) CreateShipment(ctx *gin.Context) {
	var req request.ShipmentCreationForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.CreateShipment(ctx.Request.Context(), &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) UpdateShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.ShipmentUpdateForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.UpdateShipment(ctx.Request.Context(), shipmentId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) ImportShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.ImportShipment(ctx.Request.Context(), shipmentId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) DeleteShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.DeleteShipment(ctx.Request.Context(), shipmentId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) AddItemToShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.InventoryItemDetailForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.AddItemToShipment(ctx.Request.Context(), shipmentId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) UpdateItemInShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	itemId := ctx.Param("itemId")
	if shipmentId == "" || itemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	var req request.InventoryItemDetailUpdateForm
	if err := ctx.ShouldBindJSON(&req); err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.UpdateItemInShipment(ctx.Request.Context(), shipmentId, itemId, &req)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) DeleteItemFromShipment(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	itemId := ctx.Param("itemId")
	if shipmentId == "" || itemId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.DeleteItemFromShipment(ctx.Request.Context(), shipmentId, itemId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) GetShipmentDetail(ctx *gin.Context) {
	shipmentId := ctx.Param("shipmentId")
	if shipmentId == "" {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	res, err := s.shipmentService.GetShipmentDetail(ctx.Request.Context(), shipmentId)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func (s *ShipmentController) GetShipments(ctx *gin.Context) {
	page, pageSize, valid := utils.ValidatePaginationParams(ctx)
	if !valid {
		utils.AbortErrorHandle(ctx, constant.GeneralBadRequest)
		return
	}

	sortBy := utils.ParseStringQuery(ctx, "sortBy")
	sortDirection := utils.ParseStringQuery(ctx, "sortDirection")
	query := utils.ParseStringQuery(ctx, "query")
	statusId := utils.ParseStringQuery(ctx, "statusId")
	shipmentTypeId := utils.ParseStringQuery(ctx, "shipmentTypeId")
	toCustomerId := utils.ParseStringQuery(ctx, "toCustomerId")
	fromSupplierId := utils.ParseStringQuery(ctx, "fromSupplierId")
	orderId := utils.ParseStringQuery(ctx, "orderId")

	sortByStr := ""
	if sortBy != nil {
		sortByStr = *sortBy
	}
	sortDirStr := ""
	if sortDirection != nil {
		sortDirStr = *sortDirection
	}
	queryVal := ""
	if query != nil {
		queryVal = *query
	}
	statusIdVal := ""
	if statusId != nil {
		statusIdVal = *statusId
	}
	shipmentTypeIdVal := ""
	if shipmentTypeId != nil {
		shipmentTypeIdVal = *shipmentTypeId
	}
	toCustomerIdVal := ""
	if toCustomerId != nil {
		toCustomerIdVal = *toCustomerId
	}
	fromSupplierIdVal := ""
	if fromSupplierId != nil {
		fromSupplierIdVal = *fromSupplierId
	}
	orderIdVal := ""
	if orderId != nil {
		orderIdVal = *orderId
	}

	res, err := s.shipmentService.GetShipments(
		ctx.Request.Context(),
		page, pageSize, sortByStr, sortDirStr,
		queryVal, statusIdVal, shipmentTypeIdVal, toCustomerIdVal, fromSupplierIdVal, orderIdVal,
	)
	if err != nil {
		utils.AbortErrorHandle(ctx, constant.GeneralUnauthorized)
		return
	}
	ctx.JSON(res.Code, res)
}

func NewShipmentController(shipmentService service.IShipmentService) *ShipmentController {
	return &ShipmentController{
		shipmentService: shipmentService,
	}
}
