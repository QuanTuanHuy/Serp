/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order list filter form models
 */

import {
  formatDateTimeLocalForApi,
  normalizeFilterText,
  parseTriStateBoolean,
} from '../../components/list/listFilterUtils';
import type {
  FirstMileOrderListFilters,
  FirstMileOrderStatus,
} from '../../types';
import type { OrderStatusFilter } from './orderPageModels';

export type TriStateFilter = 'ALL' | 'YES' | 'NO';

export interface OrderFilterFormState {
  keyword: string;
  status: OrderStatusFilter;
  orderCode: string;
  customerOrderCode: string;
  senderPhone: string;
  receiverPhone: string;
  originPostOfficeCode: string;
  destinationPostOfficeCode: string;
  isConfirm: TriStateFilter;
  createdFrom: string;
  createdTo: string;
  pickupFrom: string;
  pickupTo: string;
}

export const DEFAULT_ORDER_FILTER_FORM: OrderFilterFormState = {
  keyword: '',
  status: 'ALL',
  orderCode: '',
  customerOrderCode: '',
  senderPhone: '',
  receiverPhone: '',
  originPostOfficeCode: '',
  destinationPostOfficeCode: '',
  isConfirm: 'ALL',
  createdFrom: '',
  createdTo: '',
  pickupFrom: '',
  pickupTo: '',
};

export const countActiveOrderAdvancedFilters = (
  values: OrderFilterFormState
): number => {
  let count = 0;

  if (values.orderCode.trim()) count += 1;
  if (values.customerOrderCode.trim()) count += 1;
  if (values.senderPhone.trim()) count += 1;
  if (values.receiverPhone.trim()) count += 1;
  if (values.originPostOfficeCode.trim()) count += 1;
  if (values.destinationPostOfficeCode.trim()) count += 1;
  if (values.isConfirm !== 'ALL') count += 1;
  if (values.createdFrom.trim()) count += 1;
  if (values.createdTo.trim()) count += 1;
  if (values.pickupFrom.trim()) count += 1;
  if (values.pickupTo.trim()) count += 1;

  return count;
};

export const buildOrderListFilters = (
  values: OrderFilterFormState
): FirstMileOrderListFilters => {
  const createdFrom = formatDateTimeLocalForApi(values.createdFrom);
  const createdTo = formatDateTimeLocalForApi(values.createdTo);
  const pickupFrom = formatDateTimeLocalForApi(values.pickupFrom);
  const pickupTo = formatDateTimeLocalForApi(values.pickupTo);

  if (createdFrom && createdTo && createdFrom > createdTo) {
    throw new Error('Created from must be before created to.');
  }

  if (pickupFrom && pickupTo && pickupFrom > pickupTo) {
    throw new Error('Pickup from must be before pickup to.');
  }

  return {
    keyword: normalizeFilterText(values.keyword),
    orderCode: normalizeFilterText(values.orderCode),
    customerOrderCode: normalizeFilterText(values.customerOrderCode),
    senderPhone: normalizeFilterText(values.senderPhone),
    receiverPhone: normalizeFilterText(values.receiverPhone),
    originPostOfficeCode: normalizeFilterText(values.originPostOfficeCode),
    destinationPostOfficeCode: normalizeFilterText(
      values.destinationPostOfficeCode
    ),
    status:
      values.status === 'ALL'
        ? undefined
        : (values.status as FirstMileOrderStatus),
    isConfirm: parseTriStateBoolean(values.isConfirm),
    createdFrom,
    createdTo,
    pickupFrom,
    pickupTo,
  };
};
