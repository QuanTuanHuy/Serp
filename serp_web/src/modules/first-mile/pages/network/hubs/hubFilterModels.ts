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
import type { HubListFilters, HubStatus, HubType } from '../../../types';

export type HasLocationFilter = 'ALL' | 'YES' | 'NO';

export interface HubFilterFormState {
  keyword: string;
  status: 'ALL' | HubStatus;
  code: string;
  name: string;
  hubType: 'ALL' | HubType;
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
  hubType: 'ALL',
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
  if (values.hubType !== 'ALL') count += 1;
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
    'Min hub order capacity'
  );
  const maxDailyCapacity = parseOptionalNonNegativeInteger(
    values.maxDailyCapacity,
    'Max hub order capacity'
  );
  const minCurrentLoad = parseOptionalNonNegativeInteger(
    values.minCurrentLoad,
    'Min current hub load'
  );
  const maxCurrentLoad = parseOptionalNonNegativeInteger(
    values.maxCurrentLoad,
    'Max current hub load'
  );

  validateNumericRange(
    minDailyCapacity,
    maxDailyCapacity,
    'Hub order capacity'
  );
  validateNumericRange(minCurrentLoad, maxCurrentLoad, 'Current hub load');

  return {
    keyword: normalizeFilterText(values.keyword),
    code: normalizeFilterText(values.code),
    name: normalizeFilterText(values.name),
    hubType: values.hubType === 'ALL' ? undefined : values.hubType,
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
