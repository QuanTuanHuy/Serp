/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Post office list filter models
 */

import {
  normalizeFilterText,
  parseOptionalNonNegativeInteger,
  parseTriStateBoolean,
  validateNumericRange,
} from '../../components/list/listFilterUtils';
import type { PostOfficeListFilters, PostOfficeStatus } from '../../types';

export type HasLocationFilter = 'ALL' | 'YES' | 'NO';

export interface PostOfficeFilterFormState {
  keyword: string;
  code: string;
  name: string;
  provinceCode: string;
  wardCode: string;
  status: 'ALL' | PostOfficeStatus;
  hasLocation: HasLocationFilter;
  hubId: string;
  minServiceRadiusM: string;
  maxServiceRadiusM: string;
  minDailyCapacity: string;
  maxDailyCapacity: string;
  minCurrentLoad: string;
  maxCurrentLoad: string;
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
  hubId: '',
  minServiceRadiusM: '',
  maxServiceRadiusM: '',
  minDailyCapacity: '',
  maxDailyCapacity: '',
  minCurrentLoad: '',
  maxCurrentLoad: '',
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
  if (values.hubId.trim()) count += 1;
  if (values.minServiceRadiusM.trim()) count += 1;
  if (values.maxServiceRadiusM.trim()) count += 1;
  if (values.minDailyCapacity.trim()) count += 1;
  if (values.maxDailyCapacity.trim()) count += 1;
  if (values.minCurrentLoad.trim()) count += 1;
  if (values.maxCurrentLoad.trim()) count += 1;
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
    'Min daily capacity'
  );
  const maxDailyCapacity = parseOptionalNonNegativeInteger(
    values.maxDailyCapacity,
    'Max daily capacity'
  );
  const minCurrentLoad = parseOptionalNonNegativeInteger(
    values.minCurrentLoad,
    'Min current load'
  );
  const maxCurrentLoad = parseOptionalNonNegativeInteger(
    values.maxCurrentLoad,
    'Max current load'
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
  validateNumericRange(minDailyCapacity, maxDailyCapacity, 'Daily capacity');
  validateNumericRange(minCurrentLoad, maxCurrentLoad, 'Current load');
  validateNumericRange(minPriority, maxPriority, 'Priority');

  const hubIdRaw = values.hubId.trim();
  let hubId: number | undefined;
  if (hubIdRaw) {
    const parsedHubId = Number(hubIdRaw);
    if (!Number.isInteger(parsedHubId) || parsedHubId <= 0) {
      throw new Error('Hub ID must be a positive integer.');
    }
    hubId = parsedHubId;
  }

  return {
    keyword: normalizeFilterText(values.keyword),
    code: normalizeFilterText(values.code),
    name: normalizeFilterText(values.name),
    provinceCode: normalizeFilterText(values.provinceCode),
    wardCode: normalizeFilterText(values.wardCode),
    status: values.status === 'ALL' ? undefined : values.status,
    hasLocation: parseTriStateBoolean(values.hasLocation),
    hubId,
    minServiceRadiusM,
    maxServiceRadiusM,
    minDailyCapacity,
    maxDailyCapacity,
    minCurrentLoad,
    maxCurrentLoad,
    minPriority,
    maxPriority,
  };
};
