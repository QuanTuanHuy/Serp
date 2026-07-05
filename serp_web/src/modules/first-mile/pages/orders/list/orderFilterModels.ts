/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order list filter form models
 */

import {
  formatDateTimeLocalForApi,
  normalizeFilterText,
  parseTriStateBoolean,
} from '../../../components/list/listFilterUtils';
import type {
  FirstMileOrderListFilters,
  FirstMileOrderPickupMethod,
  FirstMileOrderStatus,
} from '../../../types';
import type { OrderStatusFilter } from './orderPageModels';

export type TriStateFilter = 'ALL' | 'YES' | 'NO';
export type OrderPickupMethodFilter = 'ALL' | FirstMileOrderPickupMethod;
export type OrderUpdatedSortDirection = 'NONE' | 'asc' | 'desc';

export interface OrderFilterFormState {
  keyword: string;
  status: OrderStatusFilter;
  pickupMethod: OrderPickupMethodFilter;
  orderCode: string;
  customerOrderCode: string;
  senderKeyword: string;
  senderPhone: string;
  receiverKeyword: string;
  receiverPhone: string;
  originPostOfficeCode: string;
  destinationPostOfficeCode: string;
  isConfirm: TriStateFilter;
  createdFrom: string;
  createdTo: string;
  pickupFrom: string;
  pickupTo: string;
  updatedFrom: string;
  updatedTo: string;
  updatedSortDirection: OrderUpdatedSortDirection;
}

export const DEFAULT_ORDER_FILTER_FORM: OrderFilterFormState = {
  keyword: '',
  status: 'ALL',
  pickupMethod: 'ALL',
  orderCode: '',
  customerOrderCode: '',
  senderKeyword: '',
  senderPhone: '',
  receiverKeyword: '',
  receiverPhone: '',
  originPostOfficeCode: '',
  destinationPostOfficeCode: '',
  isConfirm: 'ALL',
  createdFrom: '',
  createdTo: '',
  pickupFrom: '',
  pickupTo: '',
  updatedFrom: '',
  updatedTo: '',
  updatedSortDirection: 'NONE',
};

export const countActiveOrderAdvancedFilters = (
  values: OrderFilterFormState
): number => {
  let count = 0;

  if (values.orderCode.trim()) count += 1;
  if (values.customerOrderCode.trim()) count += 1;
  if (values.senderKeyword.trim()) count += 1;
  if (values.senderPhone.trim()) count += 1;
  if (values.receiverKeyword.trim()) count += 1;
  if (values.receiverPhone.trim()) count += 1;
  if (values.originPostOfficeCode.trim()) count += 1;
  if (values.destinationPostOfficeCode.trim()) count += 1;
  if (values.pickupMethod !== 'ALL') count += 1;
  if (values.isConfirm !== 'ALL') count += 1;
  if (values.createdFrom.trim()) count += 1;
  if (values.createdTo.trim()) count += 1;
  if (values.pickupFrom.trim()) count += 1;
  if (values.pickupTo.trim()) count += 1;
  if (values.updatedFrom.trim()) count += 1;
  if (values.updatedTo.trim()) count += 1;
  if (values.updatedSortDirection !== 'NONE') count += 1;

  return count;
};

export const buildOrderListFilters = (
  values: OrderFilterFormState
): FirstMileOrderListFilters => {
  const createdFrom = formatDateTimeLocalForApi(values.createdFrom);
  const createdTo = formatDateTimeLocalForApi(values.createdTo);
  const pickupFrom = formatDateTimeLocalForApi(values.pickupFrom);
  const pickupTo = formatDateTimeLocalForApi(values.pickupTo);
  const updatedFrom = formatDateTimeLocalForApi(values.updatedFrom);
  const updatedTo = formatDateTimeLocalForApi(values.updatedTo);

  if (createdFrom && createdTo && createdFrom > createdTo) {
    throw new Error('Thời gian tạo từ phải nhỏ hơn thời gian tạo đến.');
  }

  if (pickupFrom && pickupTo && pickupFrom > pickupTo) {
    throw new Error(
      'Thời gian lấy hàng từ phải nhỏ hơn thời gian lấy hàng đến.'
    );
  }

  if (updatedFrom && updatedTo && updatedFrom > updatedTo) {
    throw new Error(
      'Thời gian cập nhật từ phải nhỏ hơn thời gian cập nhật đến.'
    );
  }

  return {
    keyword: normalizeFilterText(values.keyword),
    orderCode: normalizeFilterText(values.orderCode),
    customerOrderCode: normalizeFilterText(values.customerOrderCode),
    senderKeyword: normalizeFilterText(values.senderKeyword),
    senderPhone: normalizeFilterText(values.senderPhone),
    receiverKeyword: normalizeFilterText(values.receiverKeyword),
    receiverPhone: normalizeFilterText(values.receiverPhone),
    originPostOfficeCode: normalizeFilterText(values.originPostOfficeCode),
    destinationPostOfficeCode: normalizeFilterText(
      values.destinationPostOfficeCode
    ),
    status:
      values.status === 'ALL'
        ? undefined
        : (values.status as FirstMileOrderStatus),
    pickupMethod:
      values.pickupMethod === 'ALL'
        ? undefined
        : (values.pickupMethod as FirstMileOrderPickupMethod),
    isConfirm: parseTriStateBoolean(values.isConfirm),
    createdFrom,
    createdTo,
    pickupFrom,
    pickupTo,
    updatedFrom,
    updatedTo,
    sortBy:
      values.updatedSortDirection === 'NONE' ? undefined : 'updated_at',
    sortDirection:
      values.updatedSortDirection === 'NONE'
        ? undefined
        : values.updatedSortDirection,
  };
};
