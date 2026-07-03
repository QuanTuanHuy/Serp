/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing page models/helpers
 */

import type {
  BillingCalculationType,
  BillingDeliveryService,
  BillingRouteType,
  BillingSurchargeRuleCode,
  CalculateShippingFeeRequest,
  ChargeableWeightConfigAdminResponse,
  SurchargeRuleAdminResponse,
  TariffAdminResponse,
  UpsertChargeableWeightConfigRequest,
  UpsertSurchargeRuleRequest,
  UpsertTariffRequest,
} from '../../../types';

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
};

export const DELIVERY_SERVICE_OPTIONS: Array<{
  value: BillingDeliveryService;
  label: string;
  description: string;
}> = [
  {
    value: 'TIEU_CHUAN',
    label: 'Tiêu chuẩn',
    description: 'Phù hợp với đơn hàng thông thường, tối ưu chi phí.',
  },
];

const ROUTE_TYPE_LABELS: Record<BillingRouteType, string> = {
  NOI_TINH_NOI_CUM: 'Nội tỉnh cùng cụm',
  NOI_TINH_LIEN_CUM: 'Nội tỉnh liên cụm',
  NOI_MIEN: 'Nội miền',
  LIEN_MIEN: 'Liên miền',
  LIEN_MIEN_DAC_BIET: 'Liên miền đặc biệt',
};

export const getRouteTypeLabel = (value: BillingRouteType): string => {
  return ROUTE_TYPE_LABELS[value] ?? value;
};

export const normalizeCategoryLabel = (value: string): string => {
  const categoryLabels: Record<string, string> = {
    BASE: 'Phí cơ bản',
    SURCHARGE: 'Phụ phí',
  };
  const normalizedKey = value.trim().toUpperCase();
  if (categoryLabels[normalizedKey]) {
    return categoryLabels[normalizedKey];
  }

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
    throw new Error(`${fieldLabel} phải là số dương.`);
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
    throw new Error('Vui lòng chọn tỉnh/thành và phường/xã gửi, nhận.');
  }

  return {
    serviceCode: form.serviceCode,
    senderWardCode,
    receiverWardCode,
    actualWeightGram: parseRequiredPositiveNumber(
      form.actualWeightGram,
      'Trọng lượng thực'
    ),
    lengthCm: parseRequiredPositiveNumber(form.lengthCm, 'Chiều dài'),
    widthCm: parseRequiredPositiveNumber(form.widthCm, 'Chiều rộng'),
    heightCm: parseRequiredPositiveNumber(form.heightCm, 'Chiều cao'),
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
  name: 'Phụ phí vùng xa',
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

export interface ChargeableWeightConfigFormState {
  serviceCode: BillingDeliveryService;
  minDimensionCm: string;
  smallBulkyThresholdCm: string;
  baseWeightGram: string;
  stepWeightGram: string;
  maxWeightGram: string;
  volumetricDivisor: string;
}

export const DEFAULT_CHARGEABLE_WEIGHT_CONFIG_FORM: ChargeableWeightConfigFormState =
  {
    serviceCode: 'TIEU_CHUAN',
    minDimensionCm: '10',
    smallBulkyThresholdCm: '100',
    baseWeightGram: '2000',
    stepWeightGram: '500',
    maxWeightGram: '15000',
    volumetricDivisor: '5000',
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

export const chargeableWeightConfigResponseToForm = (
  config: ChargeableWeightConfigAdminResponse
): ChargeableWeightConfigFormState => ({
  serviceCode: config.serviceCode,
  minDimensionCm: toFormNumber(config.minDimensionCm),
  smallBulkyThresholdCm: toFormNumber(config.smallBulkyThresholdCm),
  baseWeightGram: toFormNumber(config.baseWeightGram),
  stepWeightGram: toFormNumber(config.stepWeightGram),
  maxWeightGram: toFormNumber(config.maxWeightGram),
  volumetricDivisor: toFormNumber(config.volumetricDivisor),
});

export const ROUTE_TYPE_OPTIONS: Array<{
  value: BillingRouteType;
  label: string;
}> = [
  { value: 'NOI_TINH_NOI_CUM', label: 'Nội tỉnh cùng cụm' },
  { value: 'NOI_TINH_LIEN_CUM', label: 'Nội tỉnh liên cụm' },
  { value: 'NOI_MIEN', label: 'Nội miền' },
  { value: 'LIEN_MIEN', label: 'Liên miền' },
  { value: 'LIEN_MIEN_DAC_BIET', label: 'Liên miền đặc biệt' },
];

export const CALCULATION_TYPE_OPTIONS: Array<{
  value: BillingCalculationType;
  label: string;
  helper: string;
}> = [
  {
    value: 'FIXED_PER_ORDER',
    label: 'Cố định / đơn',
    helper: 'Áp dụng một số tiền cố định cho mỗi đơn hàng.',
  },
  {
    value: 'FIXED_PER_KG',
    label: 'Cố định / kg',
    helper: 'Số tiền cố định nhân theo trọng lượng.',
  },
  {
    value: 'PERCENTAGE',
    label: 'Phần trăm',
    helper: 'Áp dụng tỷ lệ phần trăm trên giá trị cơ sở liên quan.',
  },
  {
    value: 'STEP_WEIGHT',
    label: 'Theo bước trọng lượng',
    helper:
      'Số tiền cơ bản tại ngưỡng ban đầu cộng thêm theo từng bước trọng lượng vượt ngưỡng.',
  },
];

export const getCalculationTypeLabel = (
  value: BillingCalculationType | string
): string => {
  return (
    CALCULATION_TYPE_OPTIONS.find((option) => option.value === value)?.label ??
    value
  );
};

export const SURCHARGE_RULE_CODE_OPTIONS: Array<{
  value: BillingSurchargeRuleCode;
  label: string;
}> = [{ value: 'VUNG_XA', label: 'Vùng xa' }];

const parseRequiredDate = (value: string, fieldLabel: string): string => {
  const normalized = value.trim();
  if (!normalized) {
    throw new Error(`${fieldLabel} là bắt buộc.`);
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
    throw new Error('Giá trị số phải là số hợp lệ.');
  }
  return parsed;
};

export const buildUpsertTariffRequest = (
  form: TariffFormState
): UpsertTariffRequest => {
  return {
    serviceCode: form.serviceCode,
    routeTypeCode: form.routeTypeCode,
    baseWeight: parseRequiredPositiveNumber(
      form.baseWeight,
      'Khối lượng cơ bản'
    ),
    basePrice: parseRequiredPositiveNumber(form.basePrice, 'Giá cơ bản'),
    stepWeight: parseRequiredPositiveNumber(
      form.stepWeight,
      'Khối lượng mỗi bước'
    ),
    stepPrice: parseRequiredPositiveNumber(form.stepPrice, 'Giá mỗi bước'),
    effectiveDate: parseRequiredDate(form.effectiveDate, 'Ngày hiệu lực'),
    expirationDate: parseOptionalDate(form.expirationDate),
  };
};

export const buildUpsertSurchargeRuleRequest = (
  form: SurchargeRuleFormState
): UpsertSurchargeRuleRequest => {
  const name = form.name.trim();
  if (!name) {
    throw new Error('Tên phụ phí là bắt buộc.');
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

export const buildUpsertChargeableWeightConfigRequest = (
  form: ChargeableWeightConfigFormState
): UpsertChargeableWeightConfigRequest => {
  const baseWeightGram = parseRequiredPositiveNumber(
    form.baseWeightGram,
    'Khối lượng gốc'
  );
  const maxWeightGram = parseRequiredPositiveNumber(
    form.maxWeightGram,
    'Khối lượng tối đa'
  );

  if (baseWeightGram >= maxWeightGram) {
    throw new Error('Khối lượng gốc phải nhỏ hơn khối lượng tối đa.');
  }

  return {
    serviceCode: form.serviceCode,
    minDimensionCm: parseRequiredPositiveNumber(
      form.minDimensionCm,
      'Kích thước tối thiểu'
    ),
    smallBulkyThresholdCm: parseRequiredPositiveNumber(
      form.smallBulkyThresholdCm,
      'Ngưỡng hàng cồng kềnh'
    ),
    baseWeightGram,
    stepWeightGram: parseRequiredPositiveNumber(
      form.stepWeightGram,
      'Nấc làm tròn'
    ),
    maxWeightGram,
    volumetricDivisor: parseRequiredPositiveNumber(
      form.volumetricDivisor,
      'Hệ số quy đổi thể tích'
    ),
  };
};
