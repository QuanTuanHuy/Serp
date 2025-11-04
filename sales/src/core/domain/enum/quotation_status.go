/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package enum

type QuotationStatus string

const (
	Draft    QuotationStatus = "DRAFT"
	Sent     QuotationStatus = "SENT"
	Accepted QuotationStatus = "ACCEPTED"
	Rejected QuotationStatus = "REJECTED"
	Expired  QuotationStatus = "EXPIRED"
)
