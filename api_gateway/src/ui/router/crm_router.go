/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package router

import (
	"github.com/gin-gonic/gin"
	"github.com/serp/api-gateway/src/ui/controller/common"
	crm "github.com/serp/api-gateway/src/ui/controller/crm"
	"github.com/serp/api-gateway/src/ui/middleware"
)

func RegisterCrmRoutes(
	group *gin.RouterGroup,
	leadController *crm.LeadController,
	opportunityController *crm.OpportunityController,
	customerController *crm.CustomerController,
	contactController *crm.ContactController,
	genericProxyController *common.GenericProxyController,
) {
	// Proxy experiment
	group.Any("/crm/api/v1/proxy/*proxyPath", genericProxyController.ProxyToCRM)

	leadsV1 := group.Group("/crm/api/v1/leads")
	{
		leadsV1.Use(middleware.AuthMiddleware()).POST("", leadController.CreateLead)
		leadsV1.Use(middleware.AuthMiddleware()).PATCH(":leadId", leadController.UpdateLead)
		leadsV1.Use(middleware.AuthMiddleware()).GET(":leadId", leadController.GetLeadByID)
		leadsV1.Use(middleware.AuthMiddleware()).GET("", leadController.GetLeads)
		leadsV1.Use(middleware.AuthMiddleware()).POST("/search", leadController.FilterLeads)
		leadsV1.Use(middleware.AuthMiddleware()).POST(":leadId/qualify", leadController.QualifyLead)
		leadsV1.Use(middleware.AuthMiddleware()).POST(":leadId/convert", leadController.ConvertLead)
		leadsV1.Use(middleware.AuthMiddleware()).DELETE(":leadId", leadController.DeleteLead)
	}

	opportunitiesV1 := group.Group("/crm/api/v1/opportunities")
	{
		opportunitiesV1.Use(middleware.AuthMiddleware()).POST("", opportunityController.CreateOpportunity)
		opportunitiesV1.Use(middleware.AuthMiddleware()).PATCH(":opportunityId", opportunityController.UpdateOpportunity)
		opportunitiesV1.Use(middleware.AuthMiddleware()).GET(":opportunityId", opportunityController.GetOpportunityByID)
		opportunitiesV1.Use(middleware.AuthMiddleware()).GET("", opportunityController.GetOpportunities)
		opportunitiesV1.Use(middleware.AuthMiddleware()).POST("/search", opportunityController.FilterOpportunities)
		opportunitiesV1.Use(middleware.AuthMiddleware()).PATCH(":opportunityId/stage", opportunityController.ChangeStage)
		opportunitiesV1.Use(middleware.AuthMiddleware()).POST(":opportunityId/close-won", opportunityController.CloseAsWon)
		opportunitiesV1.Use(middleware.AuthMiddleware()).POST(":opportunityId/close-lost", opportunityController.CloseAsLost)
		opportunitiesV1.Use(middleware.AuthMiddleware()).DELETE(":opportunityId", opportunityController.DeleteOpportunity)
	}

	customersV1 := group.Group("/crm/api/v1/customers")
	{
		customersV1.Use(middleware.AuthMiddleware()).POST("", customerController.CreateCustomer)
		customersV1.Use(middleware.AuthMiddleware()).PATCH(":customerId", customerController.UpdateCustomer)
		customersV1.Use(middleware.AuthMiddleware()).GET(":customerId", customerController.GetCustomerByID)
		customersV1.Use(middleware.AuthMiddleware()).GET("", customerController.GetCustomers)
		customersV1.Use(middleware.AuthMiddleware()).POST("/search", customerController.FilterCustomers)
		customersV1.Use(middleware.AuthMiddleware()).DELETE(":customerId", customerController.DeleteCustomer)

		contactsByCustomer := customersV1.Group(":customerId/contacts")
		contactsByCustomer.Use(middleware.AuthMiddleware())
		contactsByCustomer.POST("", contactController.CreateContact)
		contactsByCustomer.PATCH(":contactId", contactController.UpdateContact)
		contactsByCustomer.GET(":contactId", contactController.GetContactByID)
		contactsByCustomer.GET("", contactController.GetContactsByCustomer)
		contactsByCustomer.DELETE(":contactId", contactController.DeleteContact)
	}

	contactsV1 := group.Group("/crm/api/v1/contacts")
	{
		contactsV1.Use(middleware.AuthMiddleware()).GET("", contactController.GetContacts)
	}
}
