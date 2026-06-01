/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Post office filters (basic/advanced)
 */

import React from 'react';
import { Search } from 'lucide-react';
import { TmsCombobox } from '@/modules/first-mile/components';
import { Input, Label } from '@/shared/components/ui';
import {
  TmsListFilterPanel,
  type TmsFilterMode,
} from '../../../components/list';
import type { Hub, Province, Ward } from '../../../types';
import { POST_OFFICE_STATUS_OPTIONS } from '../postOfficeForm';
import type {
  HasLocationFilter,
  PostOfficeFilterFormState,
} from '../postOfficeFilterModels';

interface PostOfficeFiltersCardProps {
  filterMode: TmsFilterMode;
  filterFormValues: PostOfficeFilterFormState;
  advancedFieldCount: number;
  isFetching: boolean;
  provinceSelectOptions: Province[];
  filterWardOptions: Ward[];
  hubOptions: Hub[];
  selectedFilterProvinceCode: string;
  selectedFilterWardCode: string;
  isFetchingWardsForFilter: boolean;
  onFilterModeChange: (mode: TmsFilterMode) => void;
  onFilterFieldChange: <K extends keyof PostOfficeFilterFormState>(
    field: K,
    value: PostOfficeFilterFormState[K]
  ) => void;
  onApplyFilters: (event: React.FormEvent) => void;
  onClearFilters: () => void;
  onRefresh: () => void;
}

export const PostOfficeFiltersCard: React.FC<PostOfficeFiltersCardProps> = ({
  filterMode,
  filterFormValues,
  advancedFieldCount,
  isFetching,
  provinceSelectOptions,
  filterWardOptions,
  hubOptions,
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
    ...POST_OFFICE_STATUS_OPTIONS,
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
  const hubFilterOptions = [
    { value: 'ALL', label: 'Any hub' },
    ...hubOptions.map((hub) => ({
      value: String(hub.id),
      label: `${hub.code} - ${hub.name}`,
    })),
  ];

  return (
    <TmsListFilterPanel
      title='Search & filters'
      description='Basic search by keyword, status, and location. Advanced filters match post-offices API parameters.'
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
            <Label htmlFor='post-office-filter-keyword'>Keyword</Label>
            <div className='relative'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                id='post-office-filter-keyword'
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
            <Label htmlFor='post-office-filter-status'>Status</Label>
            <TmsCombobox
              id='post-office-filter-status'
              value={filterFormValues.status}
              onValueChange={(value) =>
                onFilterFieldChange(
                  'status',
                  value as PostOfficeFilterFormState['status']
                )
              }
              options={statusOptions}
              placeholder='All statuses'
              emptyText='No statuses found'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-province'>Province</Label>
            <TmsCombobox
              id='post-office-filter-province'
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
            <Label htmlFor='post-office-filter-ward'>Ward</Label>
            <TmsCombobox
              id='post-office-filter-ward'
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
        </>
      }
      advancedFilters={
        <>
          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-code'>Code</Label>
            <Input
              id='post-office-filter-code'
              value={filterFormValues.code}
              onChange={(event) =>
                onFilterFieldChange('code', event.target.value)
              }
              placeholder='PO-HCM-01'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-name'>Name</Label>
            <Input
              id='post-office-filter-name'
              value={filterFormValues.name}
              onChange={(event) =>
                onFilterFieldChange('name', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-has-location'>
              Has location
            </Label>
            <TmsCombobox
              id='post-office-filter-has-location'
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
            <Label htmlFor='post-office-filter-hub-id'>Hub ID</Label>
            <TmsCombobox
              id='post-office-filter-hub-id'
              value={filterFormValues.hubId || 'ALL'}
              onValueChange={(value) =>
                onFilterFieldChange('hubId', value === 'ALL' ? '' : value)
              }
              options={hubFilterOptions}
              placeholder='Any hub'
              emptyText='No hubs found'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-min-service-radius'>
              Min service radius (m)
            </Label>
            <Input
              id='post-office-filter-min-service-radius'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.minServiceRadiusM}
              onChange={(event) =>
                onFilterFieldChange('minServiceRadiusM', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-max-service-radius'>
              Max service radius (m)
            </Label>
            <Input
              id='post-office-filter-max-service-radius'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.maxServiceRadiusM}
              onChange={(event) =>
                onFilterFieldChange('maxServiceRadiusM', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-min-daily-capacity'>
              Min daily capacity
            </Label>
            <Input
              id='post-office-filter-min-daily-capacity'
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
            <Label htmlFor='post-office-filter-max-daily-capacity'>
              Max daily capacity
            </Label>
            <Input
              id='post-office-filter-max-daily-capacity'
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
            <Label htmlFor='post-office-filter-min-current-load'>
              Min current load
            </Label>
            <Input
              id='post-office-filter-min-current-load'
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
            <Label htmlFor='post-office-filter-max-current-load'>
              Max current load
            </Label>
            <Input
              id='post-office-filter-max-current-load'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.maxCurrentLoad}
              onChange={(event) =>
                onFilterFieldChange('maxCurrentLoad', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-min-priority'>
              Min priority
            </Label>
            <Input
              id='post-office-filter-min-priority'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.minPriority}
              onChange={(event) =>
                onFilterFieldChange('minPriority', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-max-priority'>
              Max priority
            </Label>
            <Input
              id='post-office-filter-max-priority'
              type='number'
              min={0}
              step={1}
              value={filterFormValues.maxPriority}
              onChange={(event) =>
                onFilterFieldChange('maxPriority', event.target.value)
              }
            />
          </div>
        </>
      }
    />
  );
};
