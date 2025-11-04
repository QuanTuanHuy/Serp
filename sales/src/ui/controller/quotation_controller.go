/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package controller

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/sales/src/core/domain/constant"
	"github.com/serp/sales/src/core/domain/dto/request"
	"github.com/serp/sales/src/core/usecase"
	"github.com/serp/sales/src/kernel/utils"
)

type QuotationController struct {
	quotationUseCase usecase.IQuotationUseCase
}

func (qc *QuotationController) CreateQuotation(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "User ID not found in context")
		return
	}

	tenantID, exists := utils.GetTenantIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Tenant ID not found in context")
		return
	}

	var req request.CreateQuotationDTO
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	quotation, err := qc.quotationUseCase.CreateQuotation(c, userID, tenantID, &req)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, quotation)
}

func (qc *QuotationController) GetQuotationByID(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "User ID not found in context")
		return
	}

	tenantID, exists := utils.GetTenantIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Tenant ID not found in context")
		return
	}

	quotationID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	quotation, err := qc.quotationUseCase.GetQuotationByID(c, userID, tenantID, quotationID)
	if err != nil {
		if err.Error() == constant.QuotationNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.QuotationNotFound)
		} else if err.Error() == constant.GetQuotationForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.GetQuotationForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, quotation)
}

func (qc *QuotationController) GetAllQuotations(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "User ID not found in context")
		return
	}

	tenantID, exists := utils.GetTenantIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Tenant ID not found in context")
		return
	}

	quotations, err := qc.quotationUseCase.GetQuotationsByUserID(c, userID, tenantID)
	if err != nil {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		return
	}
	utils.SuccessfulHandle(c, quotations)
}

func (qc *QuotationController) UpdateQuotation(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "User ID not found in context")
		return
	}

	tenantID, exists := utils.GetTenantIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Tenant ID not found in context")
		return
	}

	quotationID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	var req request.UpdateQuotationDTO
	if !utils.ValidateAndBindJSON(c, &req) {
		return
	}

	quotation, err := qc.quotationUseCase.UpdateQuotation(c, userID, tenantID, quotationID, &req)
	if err != nil {
		if err.Error() == constant.QuotationNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.QuotationNotFound)
		} else if err.Error() == constant.UpdateQuotationForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.UpdateQuotationForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, quotation)
}

func (qc *QuotationController) DeleteQuotation(c *gin.Context) {
	userID, exists := utils.GetUserIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "User ID not found in context")
		return
	}

	tenantID, exists := utils.GetTenantIDFromContext(c)
	if !exists {
		utils.AbortErrorHandleCustomMessage(c, constant.GeneralUnauthorized, "Tenant ID not found in context")
		return
	}

	quotationID, valid := utils.ValidateAndParseID(c, "id")
	if !valid {
		return
	}

	err := qc.quotationUseCase.DeleteQuotation(c, userID, tenantID, quotationID)
	if err != nil {
		if err.Error() == constant.QuotationNotFound {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralNotFound, constant.QuotationNotFound)
		} else if err.Error() == constant.DeleteQuotationForbidden {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralForbidden, constant.DeleteQuotationForbidden)
		} else {
			utils.AbortErrorHandleCustomMessage(c, constant.GeneralInternalServerError, err.Error())
		}
		return
	}
	utils.SuccessfulHandle(c, nil)
}

func NewQuotationController(quotationUseCase usecase.IQuotationUseCase) *QuotationController {
	return &QuotationController{
		quotationUseCase: quotationUseCase,
	}
}
