/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub filters (basic/advanced)
 */

import React from 'react';
import { Search } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import { Input, Label } from '@/shared/components/ui';
import {
  TmsListFilterPanel,
  type TmsFilterMode,
} from '../../../../components/list';
import type { HubType, Province, Ward } from '../../../../types';
import { HUB_STATUS_OPTIONS, HUB_TYPE_OPTIONS } from '../hubForm';
import type { HasLocationFilter, HubFilterFormState } from '../hubFilterModels';

interface HubFiltersCardProps {
  filterMode: TmsFilterMode;
  filterFormValues: HubFilterFormState;
  advancedFieldCount: number;
  isFetching: boolean;
  provinceSelectOptions: Province[];
  filterWardOptions: Ward[];
  selectedFilterProvinceCode: string;
  selectedFilterWardCode: string;
  isFetchingWardsForFilter: boolean;
  onFilterModeChange: (mode: TmsFilterMode) => void;
  onFilterFieldChange: <K extends keyof HubFilterFormState>(
    field: K,
    value: HubFilterFormState[K]
  ) => void;
  onApplyFilters: (event: React.FormEvent) => void;
  onClearFilters: () => void;
  onRefresh: () => void;
}

export const HubFiltersCard: React.FC<HubFiltersCardProps> = ({
  filterMode,
  filterFormValues,
  advancedFieldCount,
  isFetching,
  provinceSelectOptions,
  filterWardOptions,
  selectedFilterProvinceCode,
  selectedFilterWardCode,
  isFetchingWardsForFilter,
  onFilterModeChange,
  onFilterFieldChange,
  onApplyFilters,
  onClearFilters,
  onRefresh,
}) => {
  const statusOptions = [
    { value: 'ALL', label: 'All statuses' },
    ...HUB_STATUS_OPTIONS,
  ];
  const hubTypeOptions = [
    { value: 'ALL', label: 'All types' },
    ...HUB_TYPE_OPTIONS,
  ];
  const provinceOptions = [
    { value: 'ALL', label: 'All provinces' },
    ...provinceSelectOptions.flatMap((province) =>
      province.provinceCode
        ? [
            {
              value: province.provinceCode,
              label: `${province.name} (${province.provinceCode})`,
            },
          ]
        : []
    ),
  ];
  const wardOptions = [
    { value: 'ALL', label: 'All wards' },
    ...filterWardOptions.flatMap((ward) =>
      ward.wardCode
        ? [
            {
              value: ward.wardCode,
              label: `${ward.name} (${ward.wardCode})`,
            },
          ]
        : []
    ),
  ];
  const hasLocationOptions = [
    { value: 'ALL', label: 'All records' },
    { value: 'YES', label: 'Has location' },
    { value: 'NO', label: 'No location' },
  ];

  return (
    <TmsListFilterPanel
      title='Search & filters'
      description='Basic search by keyword and status. Advanced filters match the hubs API query parameters.'
      filterMode={filterMode}
      onFilterModeChange={onFilterModeChange}
      advancedFieldCount={advancedFieldCount}
      isFetching={isFetching}
      onApply={onApplyFilters}
      onClear={onClearFilters}
      onRefresh={onRefresh}
      basicFilters={
        <>
          <div className='space-y-2 sm:col-span-2'>
            <Label htmlFor='hub-filter-keyword'>Keyword</Label>
            <div className='relative'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                id='hub-filter-keyword'
                className='pl-10'
                value={filterFormValues.keyword}
                onChange={(event) =>
                  onFilterFieldChange('keyword', event.target.value)
                }
                placeholder='Code, name, address...'
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-status'>Status</Label>
            <TmsCombobox
              id='hub-filter-status'
              value={filterFormValues.status}
              onValueChange={(value) =>
                onFilterFieldChange(
                  'status',
                  value as HubFilterFormState['status']
                )
              }
              options={statusOptions}
              placeholder='All statuses'
              emptyText='No statuses found'
            />
          </div>
        </>
      }
      advancedFilters={
        <>
          <div className='space-y-2'>
            <Label htmlFor='hub-filter-code'>Code</Label>
            <Input
              id='hub-filter-code'
              value={filterFormValues.code}
              onChange={(event) =>
                onFilterFieldChange('code', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-name'>Name</Label>
            <Input
              id='hub-filter-name'
              value={filterFormValues.name}
              onChange={(event) =>
                onFilterFieldChange('name', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-hub-type'>Hub type</Label>
            <TmsCombobox
              id='hub-filter-hub-type'
              value={filterFormValues.hubType}
              onValueChange={(value) =>
                onFilterFieldChange('hubType', value as 'ALL' | HubType)
              }
              options={hubTypeOptions}
              placeholder='All types'
              emptyText='No hub types found'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-province'>Province</Label>
            <TmsCombobox
              id='hub-filter-province'
              value={selectedFilterProvinceCode || 'ALL'}
              onValueChange={(value) => {
                const nextProvinceCode = value === 'ALL' ? '' : value;
                onFilterFieldChange('provinceCode', nextProvinceCode);
                onFilterFieldChange('wardCode', '');
              }}
              options={provinceOptions}
              placeholder='All provinces'
              emptyText='No provinces found'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-ward'>Ward</Label>
            <TmsCombobox
              id='hub-filter-ward'
              value={selectedFilterWardCode || 'ALL'}
              onValueChange={(value) =>
                onFilterFieldChange('wardCode', value === 'ALL' ? '' : value)
              }
              options={wardOptions}
              placeholder={
                selectedFilterProvinceCode
                  ? 'All wards'
                  : 'Select province first'
              }
              emptyText={
                isFetchingWardsForFilter
                  ? 'Loading wards...'
                  : 'No wards available'
              }
              disabled={!selectedFilterProvinceCode}
              loading={isFetchingWardsForFilter}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-has-location'>Has location</Label>
            <TmsCombobox
              id='hub-filter-has-location'
              value={filterFormValues.hasLocation}
              onValueChange={(value) =>
                onFilterFieldChange('hasLocation', value as HasLocationFilter)
              }
              options={hasLocationOptions}
              placeholder='All records'
              emptyText='No options found'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-min-daily-capacity'>
              Min hub order capacity
            </Label>
            <Input
              id='hub-filter-min-daily-capacity'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.minDailyCapacity}
              onChange={(event) =>
                onFilterFieldChange('minDailyCapacity', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-max-daily-capacity'>
              Max hub order capacity
            </Label>
            <Input
              id='hub-filter-max-daily-capacity'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.maxDailyCapacity}
              onChange={(event) =>
                onFilterFieldChange('maxDailyCapacity', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-min-current-load'>
              Min current hub load
            </Label>
            <Input
              id='hub-filter-min-current-load'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.minCurrentLoad}
              onChange={(event) =>
                onFilterFieldChange('minCurrentLoad', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-max-current-load'>
              Max current hub load
            </Label>
            <Input
              id='hub-filter-max-current-load'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.maxCurrentLoad}
              onChange={(event) =>
                onFilterFieldChange('maxCurrentLoad', event.target.value)
              }
            />
          </div>
        </>
      }
    />
  );
};
