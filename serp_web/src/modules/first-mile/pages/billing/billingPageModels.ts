/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing page models/helpers
 */

import type {
  BillingCalculationType,
  BillingDeliveryService,
  BillingRouteType,
  BillingSurchargeRuleCode,
  BillingVasRuleCode,
  CalculateShippingFeeRequest,
  SurchargeRuleAdminResponse,
  TariffAdminResponse,
  UpsertSurchargeRuleRequest,
  UpsertTariffRequest,
  UpsertVasRuleRequest,
  VasRuleAdminResponse,
} from '../../types';

export interface BillingFormState {
  serviceCode: BillingDeliveryService;
  senderProvinceCode: string;
  senderWardCode: string;
  receiverProvinceCode: string;
  receiverWardCode: string;
  actualWeightGram: string;
  lengthCm: string;
  widthCm: string;
  heightCm: string;
  codAmount: string;
  declaredValue: string;
}

export interface BillingSelectOption {
  value: string;
  label: string;
}

export const DEFAULT_BILLING_FORM: BillingFormState = {
  serviceCode: 'TIEU_CHUAN',
  senderProvinceCode: '',
  senderWardCode: '',
  receiverProvinceCode: '',
  receiverWardCode: '',
  actualWeightGram: '',
  lengthCm: '',
  widthCm: '',
  heightCm: '',
  codAmount: '',
  declaredValue: '',
};

export const DELIVERY_SERVICE_OPTIONS: Array<{
  value: BillingDeliveryService;
  label: string;
  description: string;
}> = [
  {
    value: 'TIEU_CHUAN',
    label: 'Standard',
    description: 'Suitable for regular shipments with optimized cost.',
  },
  {
    value: 'HOA_TOC',
    label: 'Express',
    description: 'Prioritizes faster delivery speed.',
  },
];

const ROUTE_TYPE_LABELS: Record<BillingRouteType, string> = {
  NOI_TINH_NOI_CUM: 'Intra-province (same cluster)',
  NOI_TINH_LIEN_CUM: 'Intra-province (cross-cluster)',
  NOI_MIEN: 'Intra-region',
  LIEN_MIEN: 'Inter-region',
  LIEN_MIEN_DAC_BIET: 'Special inter-region',
};

export const getRouteTypeLabel = (value: BillingRouteType): string => {
  return ROUTE_TYPE_LABELS[value] ?? value;
};

export const normalizeCategoryLabel = (value: string): string => {
  return value
    .trim()
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (char) => char.toUpperCase());
};

const parseRequiredPositiveNumber = (
  value: string,
  fieldLabel: string
): number => {
  const normalized = value.trim();
  const parsed = Number(normalized);

  if (!normalized || !Number.isFinite(parsed) || parsed <= 0) {
    throw new Error(`${fieldLabel} must be a positive number.`);
  }

  return parsed;
};

const parseOptionalNonNegativeNumber = (value: string): number | undefined => {
  const normalized = value.trim();

  if (!normalized) {
    return undefined;
  }

  const parsed = Number(normalized);
  if (!Number.isFinite(parsed) || parsed < 0) {
    throw new Error(
      'COD amount and declared value must be numbers greater than or equal to 0.'
    );
  }

  return parsed;
};

export const buildCalculateRequest = (
  form: BillingFormState
): CalculateShippingFeeRequest => {
  const senderProvinceCode = form.senderProvinceCode.trim();
  const senderWardCode = form.senderWardCode.trim();
  const receiverProvinceCode = form.receiverProvinceCode.trim();
  const receiverWardCode = form.receiverWardCode.trim();

  if (
    !senderProvinceCode ||
    !senderWardCode ||
    !receiverProvinceCode ||
    !receiverWardCode
  ) {
    throw new Error('Please select sender/receiver province and ward.');
  }

  return {
    serviceCode: form.serviceCode,
    senderWardCode,
    receiverWardCode,
    actualWeightGram: parseRequiredPositiveNumber(
      form.actualWeightGram,
      'Actual weight'
    ),
    lengthCm: parseRequiredPositiveNumber(form.lengthCm, 'Length'),
    widthCm: parseRequiredPositiveNumber(form.widthCm, 'Width'),
    heightCm: parseRequiredPositiveNumber(form.heightCm, 'Height'),
    codAmount: parseOptionalNonNegativeNumber(form.codAmount),
    declaredValue: parseOptionalNonNegativeNumber(form.declaredValue),
  };
};

export const formatMoneyVnd = (amount?: number): string => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount ?? 0);
};

export interface TariffFormState {
  serviceCode: BillingDeliveryService;
  routeTypeCode: BillingRouteType;
  baseWeight: string;
  basePrice: string;
  stepWeight: string;
  stepPrice: string;
  effectiveDate: string;
  expirationDate: string;
}

export const DEFAULT_TARIFF_FORM: TariffFormState = {
  serviceCode: 'TIEU_CHUAN',
  routeTypeCode: 'NOI_TINH_NOI_CUM',
  baseWeight: '3000',
  basePrice: '16500',
  stepWeight: '500',
  stepPrice: '2500',
  effectiveDate: '2025-07-10',
  expirationDate: '',
};

export interface SurchargeRuleFormState {
  code: BillingSurchargeRuleCode;
  name: string;
  calculationType: BillingCalculationType;
  ratePercent: string;
  fixedAmount: string;
  minAmount: string;
  baseWeight: string;
  basePrice: string;
  stepWeight: string;
  stepPrice: string;
  effectiveDate: string;
  expirationDate: string;
}

export const DEFAULT_SURCHARGE_FORM: SurchargeRuleFormState = {
  code: 'VUNG_XA',
  name: 'Remote area surcharge',
  calculationType: 'STEP_WEIGHT',
  ratePercent: '',
  fixedAmount: '',
  minAmount: '',
  baseWeight: '5000',
  basePrice: '7000',
  stepWeight: '500',
  stepPrice: '500',
  effectiveDate: '2025-07-10',
  expirationDate: '',
};

export interface VasRuleFormState {
  code: BillingVasRuleCode;
  name: string;
  calculationType: BillingCalculationType;
  ratePercent: string;
  fixedAmount: string;
  minAmount: string;
}

export const DEFAULT_VAS_FORM: VasRuleFormState = {
  code: 'COD',
  name: 'COD fee',
  calculationType: 'FIXED_PER_ORDER',
  ratePercent: '',
  fixedAmount: '0',
  minAmount: '',
};

const toFormNumber = (value?: number): string => {
  if (value === undefined || value === null) {
    return '';
  }
  return String(value);
};

export const tariffResponseToForm = (
  tariff: TariffAdminResponse
): TariffFormState => ({
  serviceCode: tariff.serviceCode,
  routeTypeCode: tariff.routeTypeCode,
  baseWeight: toFormNumber(tariff.baseWeight),
  basePrice: toFormNumber(tariff.basePrice),
  stepWeight: toFormNumber(tariff.stepWeight),
  stepPrice: toFormNumber(tariff.stepPrice),
  effectiveDate: tariff.effectiveDate ?? '',
  expirationDate: tariff.expirationDate ?? '',
});

export const surchargeResponseToForm = (
  rule: SurchargeRuleAdminResponse
): SurchargeRuleFormState => ({
  code: rule.code,
  name: rule.name,
  calculationType: rule.calculationType,
  ratePercent: toFormNumber(rule.ratePercent),
  fixedAmount: toFormNumber(rule.fixedAmount),
  minAmount: toFormNumber(rule.minAmount),
  baseWeight: toFormNumber(rule.baseWeight),
  basePrice: toFormNumber(rule.basePrice),
  stepWeight: toFormNumber(rule.stepWeight),
  stepPrice: toFormNumber(rule.stepPrice),
  effectiveDate: rule.effectiveDate ?? '',
  expirationDate: rule.expirationDate ?? '',
});

export const vasResponseToForm = (
  rule: VasRuleAdminResponse
): VasRuleFormState => ({
  code: rule.code,
  name: rule.name,
  calculationType: rule.calculationType,
  ratePercent: toFormNumber(rule.ratePercent),
  fixedAmount: toFormNumber(rule.fixedAmount),
  minAmount: toFormNumber(rule.minAmount),
});

export const ROUTE_TYPE_OPTIONS: Array<{
  value: BillingRouteType;
  label: string;
}> = [
  { value: 'NOI_TINH_NOI_CUM', label: 'Intra-province (same cluster)' },
  { value: 'NOI_TINH_LIEN_CUM', label: 'Intra-province (cross-cluster)' },
  { value: 'NOI_MIEN', label: 'Intra-region' },
  { value: 'LIEN_MIEN', label: 'Inter-region' },
  { value: 'LIEN_MIEN_DAC_BIET', label: 'Special inter-region' },
];

export const CALCULATION_TYPE_OPTIONS: Array<{
  value: BillingCalculationType;
  label: string;
  helper: string;
}> = [
  {
    value: 'FIXED_PER_ORDER',
    label: 'Fixed / Order',
    helper: 'A fixed amount applied once per shipment.',
  },
  {
    value: 'FIXED_PER_KG',
    label: 'Fixed / Kg',
    helper: 'A fixed amount multiplied by weight.',
  },
  {
    value: 'PERCENTAGE',
    label: 'Percentage',
    helper: 'A percentage applied on the relevant base value.',
  },
  {
    value: 'STEP_WEIGHT',
    label: 'Step Weight',
    helper:
      'Base amount at a threshold plus step amount for each extra weight step.',
  },
];

export const SURCHARGE_RULE_CODE_OPTIONS: Array<{
  value: BillingSurchargeRuleCode;
  label: string;
}> = [
  { value: 'VUNG_XA', label: 'Remote Area' },
  { value: 'HANG_GIA_TRI_CAO', label: 'High Value Item' },
  { value: 'CHUNG_TU_QUAN_TRONG', label: 'Important Document' },
  { value: 'DE_VO', label: 'Fragile Item' },
  { value: 'QUA_KHO', label: 'Oversized Item' },
  { value: 'CHAT_LONG', label: 'Liquid Item' },
];

export const VAS_RULE_CODE_OPTIONS: Array<{
  value: BillingVasRuleCode;
  label: string;
}> = [
  { value: 'COD', label: 'Cash On Delivery' },
  { value: 'BAO_HIEM', label: 'Insurance' },
];

const parseRequiredDate = (value: string, fieldLabel: string): string => {
  const normalized = value.trim();
  if (!normalized) {
    throw new Error(`${fieldLabel} is required.`);
  }
  return normalized;
};

const parseOptionalDate = (value: string): string | undefined => {
  const normalized = value.trim();
  return normalized || undefined;
};

export const parseOptionalNumber = (value: string): number | undefined => {
  const normalized = value.trim();
  if (!normalized) {
    return undefined;
  }

  const parsed = Number(normalized);
  if (!Number.isFinite(parsed)) {
    throw new Error('Numeric values must be valid numbers.');
  }
  return parsed;
};

export const buildUpsertTariffRequest = (
  form: TariffFormState
): UpsertTariffRequest => {
  return {
    serviceCode: form.serviceCode,
    routeTypeCode: form.routeTypeCode,
    baseWeight: parseRequiredPositiveNumber(form.baseWeight, 'Base weight'),
    basePrice: parseRequiredPositiveNumber(form.basePrice, 'Base price'),
    stepWeight: parseRequiredPositiveNumber(form.stepWeight, 'Step weight'),
    stepPrice: parseRequiredPositiveNumber(form.stepPrice, 'Step price'),
    effectiveDate: parseRequiredDate(form.effectiveDate, 'Effective date'),
    expirationDate: parseOptionalDate(form.expirationDate),
  };
};

export const buildUpsertSurchargeRuleRequest = (
  form: SurchargeRuleFormState
): UpsertSurchargeRuleRequest => {
  const name = form.name.trim();
  if (!name) {
    throw new Error('Surcharge name is required.');
  }

  return {
    code: form.code,
    name,
    calculationType: form.calculationType,
    ratePercent: parseOptionalNumber(form.ratePercent),
    fixedAmount: parseOptionalNumber(form.fixedAmount),
    minAmount: parseOptionalNumber(form.minAmount),
    baseWeight: parseOptionalNumber(form.baseWeight),
    basePrice: parseOptionalNumber(form.basePrice),
    stepWeight: parseOptionalNumber(form.stepWeight),
    stepPrice: parseOptionalNumber(form.stepPrice),
    effectiveDate: parseOptionalDate(form.effectiveDate),
    expirationDate: parseOptionalDate(form.expirationDate),
  };
};

export const buildUpsertVasRuleRequest = (
  form: VasRuleFormState
): UpsertVasRuleRequest => {
  const name = form.name.trim();
  if (!name) {
    throw new Error('VAS name is required.');
  }

  return {
    code: form.code,
    name,
    calculationType: form.calculationType,
    ratePercent: parseOptionalNumber(form.ratePercent),
    fixedAmount: parseOptionalNumber(form.fixedAmount),
    minAmount: parseOptionalNumber(form.minAmount),
  };
};
