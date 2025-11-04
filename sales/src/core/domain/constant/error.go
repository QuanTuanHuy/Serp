/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package constant

// HTTP Error Codes
const (
	GeneralInternalServerError = 500

	GeneralBadRequest = 400

	GeneralUnauthorized = 401

	GeneralForbidden = 403

	GeneralNotFound = 404

	GeneralSuccess = 200
)

const (
	MessageOK = "OK"
)

const (
	HttpStatusSuccess = "success"
	HttpStatusError   = "error"
)

// Business Error Messages - Quotation
const (
	QuotationNotFound        = "Quotation not found"
	GetQuotationForbidden    = "You don't have permission to access this quotation"
	UpdateQuotationForbidden = "You don't have permission to update this quotation"
	DeleteQuotationForbidden = "You don't have permission to delete this quotation"
)
