/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub list filter models
 */

import {
  normalizeFilterText,
  parseOptionalNonNegativeInteger,
  parseTriStateBoolean,
  validateNumericRange,
} from '../../../components/list/listFilterUtils';
import type { HubListFilters, HubStatus } from '../../../types';

export type HasLocationFilter = 'ALL' | 'YES' | 'NO';

export interface HubFilterFormState {
  keyword: string;
  status: 'ALL' | HubStatus;
  code: string;
  name: string;
  provinceCode: string;
  wardCode: string;
  hasLocation: HasLocationFilter;
  minDailyCapacity: string;
  maxDailyCapacity: string;
  minCurrentLoad: string;
  maxCurrentLoad: string;
}

export const DEFAULT_HUB_FILTER_FORM: HubFilterFormState = {
  keyword: '',
  status: 'ALL',
  code: '',
  name: '',
  provinceCode: '',
  wardCode: '',
  hasLocation: 'ALL',
  minDailyCapacity: '',
  maxDailyCapacity: '',
  minCurrentLoad: '',
  maxCurrentLoad: '',
};

export const countActiveHubAdvancedFilters = (
  values: HubFilterFormState
): number => {
  let count = 0;

  if (values.code.trim()) count += 1;
  if (values.name.trim()) count += 1;
  if (values.provinceCode.trim()) count += 1;
  if (values.wardCode.trim()) count += 1;
  if (values.hasLocation !== 'ALL') count += 1;
  if (values.minDailyCapacity.trim()) count += 1;
  if (values.maxDailyCapacity.trim()) count += 1;
  if (values.minCurrentLoad.trim()) count += 1;
  if (values.maxCurrentLoad.trim()) count += 1;

  return count;
};

export const buildHubListFilters = (
  values: HubFilterFormState
): HubListFilters => {
  const minDailyCapacity = parseOptionalNonNegativeInteger(
    values.minDailyCapacity,
    'Sức chứa đơn tối thiểu tại hub'
  );
  const maxDailyCapacity = parseOptionalNonNegativeInteger(
    values.maxDailyCapacity,
    'Sức chứa đơn tối đa tại hub'
  );
  const minCurrentLoad = parseOptionalNonNegativeInteger(
    values.minCurrentLoad,
    'Tải hub tối thiểu'
  );
  const maxCurrentLoad = parseOptionalNonNegativeInteger(
    values.maxCurrentLoad,
    'Tải hub tối đa'
  );

  validateNumericRange(
    minDailyCapacity,
    maxDailyCapacity,
    'Sức chứa đơn tại hub'
  );
  validateNumericRange(minCurrentLoad, maxCurrentLoad, 'Tải hiện tại của hub');

  return {
    keyword: normalizeFilterText(values.keyword),
    code: normalizeFilterText(values.code),
    name: normalizeFilterText(values.name),
    provinceCode: normalizeFilterText(values.provinceCode),
    wardCode: normalizeFilterText(values.wardCode),
    status: values.status === 'ALL' ? undefined : values.status,
    hasLocation: parseTriStateBoolean(values.hasLocation),
    minDailyCapacity,
    maxDailyCapacity,
    minCurrentLoad,
    maxCurrentLoad,
  };
};
