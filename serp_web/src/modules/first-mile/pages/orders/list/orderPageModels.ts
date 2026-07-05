/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order page model and helpers
 */

import type {
  CreateOrderRequest,
  FirstMileDeliveryRequestTime,
  FirstMileFeePayer,
  FirstMileOrderDetail,
  FirstMileOrderPickupMethod,
  FirstMileOrderProductCategory,
  FirstMileOrderProductItem,
  FirstMileOrderStatus,
  FirstMileOrderType,
  OrderImportItem,
  Province,
  Ward,
} from '../../../types';

export const PAGE_SIZE = 20;
export const IMPORT_PREVIEW_LIMIT = 5;

export type OrderAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_POST_OFFICE'
  | 'COURIER_ASSIGNED'
  | 'CUSTOMER_CREATED'
  | 'NO_ACCESS';

export type LocationTarget = 'sender' | 'receiver';
export type OrderFormMode = 'create' | 'edit';

type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline';

export type OrderStatusFilter = 'ALL' | FirstMileOrderStatus;

export const ORDER_STATUS_OPTIONS: FirstMileOrderStatus[] = [
  'CREATED',
  'ASSIGNED_TO_PICKUP',
  'PICKING_UP',
  'PICKUP_FAILED',
  'PICKED_UP',
  'PENDING_ORIGIN_POST_OFFICE_INBOUND',
  'AT_ORIGIN_POST_OFFICE',
  'CANCELLED',
  'LOST_OR_DAMAGED',
];

export const normalizeLocationCode = (value?: string): string =>
  value?.trim() ?? '';

export const buildWardSelectOptions = (
  wards: Ward[] | undefined,
  selectedWardCode: string,
  provinceCode: string
): Ward[] => {
  const options = [...(wards ?? [])];

  if (
    selectedWardCode &&
    !options.some(
      (ward) => normalizeLocationCode(ward.wardCode) === selectedWardCode
    )
  ) {
    options.unshift({
      wardCode: selectedWardCode,
      name: selectedWardCode,
      provinceCode,
    });
  }

  return options;
};

export interface CreateOrderFormState {
  customerOrderCode: string;
  senderName: string;
  senderPhone: string;
  senderProvinceCode: string;
  senderWardCode: string;
  senderAddressDetail: string;
  senderLatitude: string;
  senderLongitude: string;
  receiverName: string;
  receiverPhone: string;
  receiverProvinceCode: string;
  receiverWardCode: string;
  receiverAddressDetail: string;
  receiverLatitude: string;
  receiverLongitude: string;
  pickupTimeStart: string;
  pickupTimeEnd: string;
  deliveryRequestTime: FirstMileDeliveryRequestTime;
  pickupMethod: FirstMileOrderPickupMethod;
  orderType: FirstMileOrderType;
  orderProductCategory: FirstMileOrderProductCategory;
  feePayer: FirstMileFeePayer;
  isCod: 'true' | 'false';
  dimensionLengthCm: string;
  dimensionWidthCm: string;
  dimensionHeightCm: string;
  totalVolumeM3: string;
  note: string;
}

export type UpdateOrderFormField = <K extends keyof CreateOrderFormState>(
  field: K,
  value: CreateOrderFormState[K]
) => void;

export const DEFAULT_CREATE_ORDER_FORM: CreateOrderFormState = {
  customerOrderCode: '',
  senderName: '',
  senderPhone: '',
  senderProvinceCode: '',
  senderWardCode: '',
  senderAddressDetail: '',
  senderLatitude: '',
  senderLongitude: '',
  receiverName: '',
  receiverPhone: '',
  receiverProvinceCode: '',
  receiverWardCode: '',
  receiverAddressDetail: '',
  receiverLatitude: '',
  receiverLongitude: '',
  pickupTimeStart: '',
  pickupTimeEnd: '',
  deliveryRequestTime: 'BUSINESS_HOURS',
  pickupMethod: 'COURIER_PICKUP',
  orderType: 'STANDARD_ORDER',
  orderProductCategory: 'SOLID',
  feePayer: 'SENDER',
  isCod: 'false',
  dimensionLengthCm: '',
  dimensionWidthCm: '',
  dimensionHeightCm: '',
  totalVolumeM3: '',
  note: '',
};

export const DELIVERY_REQUEST_TIME_OPTIONS: Array<{
  value: FirstMileDeliveryRequestTime;
  label: string;
}> = [
  { value: 'BUSINESS_HOURS', label: 'Giờ hành chính' },
  { value: 'FULL_DAY', label: 'Cả ngày' },
  { value: 'MORNING', label: 'Buổi sáng' },
  { value: 'AFTERNOON', label: 'Buổi chiều' },
  { value: 'SUNDAY', label: 'Chủ nhật' },
  { value: 'HOLIDAY', label: 'Ngày lễ' },
];

export const ORDER_PICKUP_METHOD_OPTIONS: Array<{
  value: FirstMileOrderPickupMethod;
  label: string;
}> = [
  {
    value: 'COURIER_PICKUP',
    label: 'Nhân viên lấy hàng tại địa chỉ người gửi',
  },
  {
    value: 'DROP_OFF_AT_POST_OFFICE',
    label: 'Khách hàng gửi tại bưu cục',
  },
];

export const FEE_PAYER_OPTIONS: Array<{
  value: FirstMileFeePayer;
  label: string;
}> = [
  { value: 'SENDER', label: 'Người gửi' },
  { value: 'RECEIVER', label: 'Người nhận' },
];

export const ORDER_PRODUCT_CATEGORY_OPTIONS: Array<{
  value: FirstMileOrderProductCategory;
  label: string;
}> = [
  { value: 'SOLID', label: 'Hàng thường' },
  { value: 'HIGH_VALUE', label: 'Hàng giá trị cao' },
  { value: 'FRAGILE', label: 'Hàng dễ vỡ' },
  { value: 'IMPORTANT_DOCUMENT', label: 'Chứng từ quan trọng' },
  { value: 'OVERSIZED', label: 'Hàng quá khổ' },
  { value: 'LIQUID', label: 'Chất lỏng' },
  { value: 'MAGNETIC_BATTERY', label: 'Từ tính/pin' },
];

export const getOrderProductCategoryLabel = (
  category?: FirstMileOrderProductCategory
): string => {
  if (!category) {
    return '--';
  }
  return (
    ORDER_PRODUCT_CATEGORY_OPTIONS.find((option) => option.value === category)
      ?.label ?? category
  );
};

export const resolveOrderAccessScope = (roles: string[]): OrderAccessScope => {
  if (roles.includes('TMS_ADMIN')) {
    return 'ADMIN_ALL';
  }

  if (roles.includes('TMS_POSTOFFICER_MANAGER')) {
    return 'MANAGER_POST_OFFICE';
  }

  if (roles.includes('TMS_POSTOFFICER')) {
    return 'COURIER_ASSIGNED';
  }

  if (roles.includes('TMS_CUSTOMER')) {
    return 'CUSTOMER_CREATED';
  }

  return 'NO_ACCESS';
};

export const getScopeBadgeLabel = (scope: OrderAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'Quản trị TMS';
    case 'MANAGER_POST_OFFICE':
      return 'Quản lý bưu cục';
    case 'COURIER_ASSIGNED':
      return 'Nhân viên giao nhận';
    case 'CUSTOMER_CREATED':
      return 'Khách hàng';
    default:
      return 'Không có quyền';
  }
};

export const getScopeDescription = (scope: OrderAccessScope): string => {
  switch (scope) {
    case 'ADMIN_ALL':
      return 'Bạn có thể xem toàn bộ đơn hàng trong hệ thống.';
    case 'MANAGER_POST_OFFICE':
      return 'Bạn có thể xem các đơn hàng được gán cho bưu cục mình quản lý.';
    case 'COURIER_ASSIGNED':
      return 'Bạn có thể xem các đơn hàng được gán cho mình.';
    case 'CUSTOMER_CREATED':
      return 'Bạn chỉ có thể xem các đơn hàng do chính mình tạo.';
    default:
      return 'Tài khoản hiện tại không có quyền truy cập danh sách đơn hàng.';
  }
};

export const formatStatusLabel = (status: FirstMileOrderStatus): string => {
  switch (status) {
    case 'CREATED':
      return 'Mới tạo';
    case 'ASSIGNED_TO_PICKUP':
      return 'Đã phân công lấy hàng';
    case 'PICKING_UP':
      return 'Đang lấy hàng';
    case 'PICKUP_FAILED':
      return 'Lấy hàng thất bại';
    case 'PICKED_UP':
      return 'Đã lấy hàng';
    case 'PENDING_ORIGIN_POST_OFFICE_INBOUND':
      return 'Chờ nhập bưu cục gốc';
    case 'AT_ORIGIN_POST_OFFICE':
      return 'Tại bưu cục gốc';
    case 'CANCELLED':
      return 'Đã hủy';
    case 'LOST_OR_DAMAGED':
      return 'Thất lạc / hư hỏng';
    default:
      return status.replaceAll('_', ' ');
  }
};

export const formatPickupMethodLabel = (
  pickupMethod?: FirstMileOrderPickupMethod
): string => {
  if (pickupMethod === 'DROP_OFF_AT_POST_OFFICE') {
    return 'Gửi tại bưu cục';
  }

  return 'Nhân viên lấy hàng';
};

export const getStatusBadgeVariant = (
  status: FirstMileOrderStatus
): BadgeVariant => {
  switch (status) {
    case 'CREATED':
    case 'ASSIGNED_TO_PICKUP':
      return 'secondary';
    case 'PICKING_UP':
    case 'PICKED_UP':
    case 'PENDING_ORIGIN_POST_OFFICE_INBOUND':
    case 'AT_ORIGIN_POST_OFFICE':
      return 'default';
    case 'PICKUP_FAILED':
      return 'outline';
    case 'CANCELLED':
    case 'LOST_OR_DAMAGED':
      return 'destructive';
    default:
      return 'outline';
  }
};

export const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('vi-VN');
};

export const buildOrderAddressLabel = (
  name?: string,
  phone?: string,
  addressDetail?: string
): string => {
  const mainLabel = [name, phone].filter(Boolean).join(' - ');
  if (!mainLabel && !addressDetail) {
    return '--';
  }

  if (!addressDetail) {
    return mainLabel;
  }

  return `${mainLabel || '--'} | ${addressDetail}`;
};

export const buildPostOfficeAssignmentLabel = (
  order: FirstMileOrderDetail
): string => {
  const origin = order.originPostOfficeCode || '--';
  const destination = order.destinationPostOfficeCode || '--';
  return `${origin} -> ${destination}`;
};

export const parseOptionalNumberInput = (value: string): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isFinite(parsedValue)) {
    return undefined;
  }

  return parsedValue;
};

export const parseRequiredNumberInput = (value: string): number | null => {
  const parsedValue = parseOptionalNumberInput(value);
  return parsedValue === undefined ? null : parsedValue;
};

export const toDateTimeLocalValue = (value?: string): string => {
  if (!value) {
    return '';
  }

  const parsedDate = new Date(value);
  if (!Number.isNaN(parsedDate.getTime())) {
    const timezoneOffsetInMs = parsedDate.getTimezoneOffset() * 60 * 1000;
    return new Date(parsedDate.getTime() - timezoneOffsetInMs)
      .toISOString()
      .slice(0, 16);
  }

  return value.slice(0, 16);
};

export const isDraftOrder = (order: FirstMileOrderDetail): boolean => {
  return order.status === 'CREATED' && !order.isConfirm;
};

export const isDropOffOrder = (order: FirstMileOrderDetail): boolean => {
  return order.pickupMethod === 'DROP_OFF_AT_POST_OFFICE';
};

export const mapOrderToFormState = (
  order: FirstMileOrderDetail
): CreateOrderFormState => {
  return {
    customerOrderCode: order.customerOrderCode || '',
    senderName: order.senderName || '',
    senderPhone: order.senderPhone || '',
    senderProvinceCode: order.senderProvinceCode || '',
    senderWardCode: order.senderWardCode || '',
    senderAddressDetail: order.senderAddressDetail || '',
    senderLatitude:
      order.senderLatitude === undefined || order.senderLatitude === null
        ? ''
        : String(order.senderLatitude),
    senderLongitude:
      order.senderLongitude === undefined || order.senderLongitude === null
        ? ''
        : String(order.senderLongitude),
    receiverName: order.receiverName || '',
    receiverPhone: order.receiverPhone || '',
    receiverProvinceCode: order.receiverProvinceCode || '',
    receiverWardCode: order.receiverWardCode || '',
    receiverAddressDetail: order.receiverAddressDetail || '',
    receiverLatitude:
      order.receiverLatitude === undefined || order.receiverLatitude === null
        ? ''
        : String(order.receiverLatitude),
    receiverLongitude:
      order.receiverLongitude === undefined || order.receiverLongitude === null
        ? ''
        : String(order.receiverLongitude),
    pickupTimeStart: toDateTimeLocalValue(order.pickupTimeStart),
    pickupTimeEnd: toDateTimeLocalValue(order.pickupTimeEnd),
    deliveryRequestTime: order.deliveryRequestTime || 'BUSINESS_HOURS',
    pickupMethod: order.pickupMethod || 'COURIER_PICKUP',
    orderType: order.orderType || 'STANDARD_ORDER',
    orderProductCategory: order.orderProductCategory || 'SOLID',
    feePayer: order.feePayer || 'SENDER',
    isCod:
      order.codAmount !== undefined &&
      order.codAmount !== null &&
      order.codAmount > 0
        ? 'true'
        : 'false',
    dimensionLengthCm:
      order.dimensionLengthCm === undefined || order.dimensionLengthCm === null
        ? ''
        : String(order.dimensionLengthCm),
    dimensionWidthCm:
      order.dimensionWidthCm === undefined || order.dimensionWidthCm === null
        ? ''
        : String(order.dimensionWidthCm),
    dimensionHeightCm:
      order.dimensionHeightCm === undefined || order.dimensionHeightCm === null
        ? ''
        : String(order.dimensionHeightCm),
    totalVolumeM3:
      order.totalVolume === undefined || order.totalVolume === null
        ? ''
        : String(order.totalVolume),
    note: order.note || '',
  };
};

export const mapOrderProductsToRequest = (
  products: FirstMileOrderProductItem[] | undefined
): CreateOrderRequest['products'] => {
  if (!products || products.length === 0) {
    return [];
  }

  return products
    .filter(
      (product) =>
        product.name &&
        product.productTypeId !== undefined &&
        product.value !== undefined &&
        product.quantity !== undefined &&
        product.weight !== undefined
    )
    .map((product) => ({
      name: product.name as string,
      product_type_id: product.productTypeId as number,
      value: Math.round(product.value as number),
      quantity: product.quantity as number,
      weight_gram: product.weight as number,
    }));
};

export const isConfirmableStatus = (status: FirstMileOrderStatus): boolean => {
  return status === 'CREATED' || status === 'PICKUP_FAILED';
};

export const formatOrderImportProductsPreview = (
  products?: OrderImportItem['products']
): string => {
  if (!products || products.length === 0) {
    return '-';
  }

  const previewText = products
    .slice(0, 2)
    .map((product) => {
      const productName = product.name?.trim() || '-';
      const quantity =
        product.quantity === undefined || product.quantity === null
          ? '-'
          : String(product.quantity);
      const productType =
        product.product_type_code || product.product_type_name;

      return productType
        ? `${productName} x${quantity} (${productType})`
        : `${productName} x${quantity}`;
    })
    .join('; ');

  return products.length > 2 ? `${previewText}; ...` : previewText;
};

export const getProvinceNameByCode = (
  provinces: Province[]
): Record<string, string> => {
  return provinces.reduce<Record<string, string>>((accumulator, province) => {
    const provinceCode = normalizeLocationCode(province.provinceCode);

    if (provinceCode) {
      accumulator[provinceCode] = province.name;
    }

    return accumulator;
  }, {});
};
