/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing types for TMS shipping fee
 */

export type BillingDeliveryService = 'HOA_TOC' | 'TIEU_CHUAN';

export type BillingRouteType =
  | 'NOI_TINH_NOI_CUM'
  | 'NOI_TINH_LIEN_CUM'
  | 'NOI_MIEN'
  | 'LIEN_MIEN'
  | 'LIEN_MIEN_DAC_BIET';

export type BillingCalculationType =
  | 'FIXED_PER_ORDER'
  | 'FIXED_PER_KG'
  | 'PERCENTAGE'
  | 'STEP_WEIGHT';

export type BillingSurchargeRuleCode =
  | 'VUNG_XA'
  | 'HANG_GIA_TRI_CAO'
  | 'CHUNG_TU_QUAN_TRONG'
  | 'DE_VO'
  | 'QUA_KHO'
  | 'CHAT_LONG';

export type BillingVasRuleCode = 'COD' | 'BAO_HIEM';

export interface BillingSpecialCargoRequest {
  importantDocument: boolean;
  fragile: boolean;
  liquid: boolean;
}

export interface CalculateShippingFeeRequest {
  serviceCode: BillingDeliveryService;
  senderWardCode: string;
  receiverWardCode: string;
  actualWeightGram: number;
  lengthCm: number;
  widthCm: number;
  heightCm: number;
  codAmount?: number;
  declaredValue?: number;
  specialCargo?: BillingSpecialCargoRequest;
}

export interface FeeLineItemResponse {
  code: string;
  name: string;
  category: string;
  amount: number;
}

export interface CalculateShippingFeeResponse {
  serviceCode: BillingDeliveryService;
  routeType: BillingRouteType;
  chargeableWeightGram: number;
  baseFee: number;
  surchargeFee: number;
  vasFee: number;
  totalFee: number;
  feeItems: FeeLineItemResponse[];
}

export interface UpsertTariffRequest {
  serviceCode: BillingDeliveryService;
  routeTypeCode: BillingRouteType;
  baseWeight: number;
  basePrice: number;
  stepWeight: number;
  stepPrice: number;
  effectiveDate: string;
  expirationDate?: string;
}

export interface TariffAdminResponse extends UpsertTariffRequest {
  id: number;
}

export interface UpsertSurchargeRuleRequest {
  code: BillingSurchargeRuleCode;
  name: string;
  calculationType: BillingCalculationType;
  ratePercent?: number;
  fixedAmount?: number;
  minAmount?: number;
  baseWeight?: number;
  basePrice?: number;
  stepWeight?: number;
  stepPrice?: number;
  effectiveDate?: string;
  expirationDate?: string;
}

export interface SurchargeRuleAdminResponse extends UpsertSurchargeRuleRequest {
  id: number;
}

export interface UpsertVasRuleRequest {
  code: BillingVasRuleCode;
  name: string;
  calculationType: BillingCalculationType;
  ratePercent?: number;
  fixedAmount?: number;
  minAmount?: number;
}

export interface VasRuleAdminResponse extends UpsertVasRuleRequest {
  id: number;
}
