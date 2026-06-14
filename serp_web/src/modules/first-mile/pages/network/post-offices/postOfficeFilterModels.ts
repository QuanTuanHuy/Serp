/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Post office list filter models
 */

import {
  normalizeFilterText,
  parseOptionalNonNegativeInteger,
  parseTriStateBoolean,
  validateNumericRange,
} from '../../../components/list/listFilterUtils';
import type { PostOfficeListFilters, PostOfficeStatus } from '../../../types';

export type HasLocationFilter = 'ALL' | 'YES' | 'NO';

export interface PostOfficeFilterFormState {
  keyword: string;
  code: string;
  name: string;
  provinceCode: string;
  wardCode: string;
  status: 'ALL' | PostOfficeStatus;
  hasLocation: HasLocationFilter;
  minServiceRadiusM: string;
  maxServiceRadiusM: string;
  minDailyCapacity: string;
  maxDailyCapacity: string;
  minCurrentLoad: string;
  maxCurrentLoad: string;
  minDeliveryCapacity: string;
  maxDeliveryCapacity: string;
  minCurrentDeliveryLoad: string;
  maxCurrentDeliveryLoad: string;
  minPriority: string;
  maxPriority: string;
}

export const DEFAULT_POST_OFFICE_FILTER_FORM: PostOfficeFilterFormState = {
  keyword: '',
  code: '',
  name: '',
  provinceCode: '',
  wardCode: '',
  status: 'ALL',
  hasLocation: 'ALL',
  minServiceRadiusM: '',
  maxServiceRadiusM: '',
  minDailyCapacity: '',
  maxDailyCapacity: '',
  minCurrentLoad: '',
  maxCurrentLoad: '',
  minDeliveryCapacity: '',
  maxDeliveryCapacity: '',
  minCurrentDeliveryLoad: '',
  maxCurrentDeliveryLoad: '',
  minPriority: '',
  maxPriority: '',
};

export const countActivePostOfficeAdvancedFilters = (
  values: PostOfficeFilterFormState
): number => {
  let count = 0;

  if (values.code.trim()) count += 1;
  if (values.name.trim()) count += 1;
  if (values.hasLocation !== 'ALL') count += 1;
  if (values.minServiceRadiusM.trim()) count += 1;
  if (values.maxServiceRadiusM.trim()) count += 1;
  if (values.minDailyCapacity.trim()) count += 1;
  if (values.maxDailyCapacity.trim()) count += 1;
  if (values.minCurrentLoad.trim()) count += 1;
  if (values.maxCurrentLoad.trim()) count += 1;
  if (values.minDeliveryCapacity.trim()) count += 1;
  if (values.maxDeliveryCapacity.trim()) count += 1;
  if (values.minCurrentDeliveryLoad.trim()) count += 1;
  if (values.maxCurrentDeliveryLoad.trim()) count += 1;
  if (values.minPriority.trim()) count += 1;
  if (values.maxPriority.trim()) count += 1;

  return count;
};

export const buildPostOfficeListFilters = (
  values: PostOfficeFilterFormState
): PostOfficeListFilters => {
  const minServiceRadiusM = parseOptionalNonNegativeInteger(
    values.minServiceRadiusM,
    'Min service radius'
  );
  const maxServiceRadiusM = parseOptionalNonNegativeInteger(
    values.maxServiceRadiusM,
    'Max service radius'
  );
  const minDailyCapacity = parseOptionalNonNegativeInteger(
    values.minDailyCapacity,
    'Min pickup order capacity'
  );
  const maxDailyCapacity = parseOptionalNonNegativeInteger(
    values.maxDailyCapacity,
    'Max pickup order capacity'
  );
  const minCurrentLoad = parseOptionalNonNegativeInteger(
    values.minCurrentLoad,
    'Min current pickup load'
  );
  const maxCurrentLoad = parseOptionalNonNegativeInteger(
    values.maxCurrentLoad,
    'Max current pickup load'
  );
  const minDeliveryCapacity = parseOptionalNonNegativeInteger(
    values.minDeliveryCapacity,
    'Min delivery capacity'
  );
  const maxDeliveryCapacity = parseOptionalNonNegativeInteger(
    values.maxDeliveryCapacity,
    'Max delivery capacity'
  );
  const minCurrentDeliveryLoad = parseOptionalNonNegativeInteger(
    values.minCurrentDeliveryLoad,
    'Min current delivery load'
  );
  const maxCurrentDeliveryLoad = parseOptionalNonNegativeInteger(
    values.maxCurrentDeliveryLoad,
    'Max current delivery load'
  );
  const minPriority = parseOptionalNonNegativeInteger(
    values.minPriority,
    'Min priority'
  );
  const maxPriority = parseOptionalNonNegativeInteger(
    values.maxPriority,
    'Max priority'
  );

  validateNumericRange(minServiceRadiusM, maxServiceRadiusM, 'Service radius');
  validateNumericRange(
    minDailyCapacity,
    maxDailyCapacity,
    'Pickup order capacity'
  );
  validateNumericRange(minCurrentLoad, maxCurrentLoad, 'Current pickup load');
  validateNumericRange(
    minDeliveryCapacity,
    maxDeliveryCapacity,
    'Delivery capacity'
  );
  validateNumericRange(
    minCurrentDeliveryLoad,
    maxCurrentDeliveryLoad,
    'Current delivery load'
  );
  validateNumericRange(minPriority, maxPriority, 'Priority');

  return {
    keyword: normalizeFilterText(values.keyword),
    code: normalizeFilterText(values.code),
    name: normalizeFilterText(values.name),
    provinceCode: normalizeFilterText(values.provinceCode),
    wardCode: normalizeFilterText(values.wardCode),
    status: values.status === 'ALL' ? undefined : values.status,
    hasLocation: parseTriStateBoolean(values.hasLocation),
    minServiceRadiusM,
    maxServiceRadiusM,
    minDailyCapacity,
    maxDailyCapacity,
    minCurrentLoad,
    maxCurrentLoad,
    minDeliveryCapacity,
    maxDeliveryCapacity,
    minCurrentDeliveryLoad,
    maxCurrentDeliveryLoad,
    minPriority,
    maxPriority,
  };
};
