/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub filters (basic/advanced)
 */

import React from 'react';
import {
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { Search } from 'lucide-react';
import {
  TmsListFilterPanel,
  type TmsFilterMode,
} from '../../../components/list';
import type { HubType, Province, Ward } from '../../../types';
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
            <Select
              value={filterFormValues.status}
              onValueChange={(value) =>
                onFilterFieldChange(
                  'status',
                  value as HubFilterFormState['status']
                )
              }
            >
              <SelectTrigger id='hub-filter-status' className='w-full'>
                <SelectValue placeholder='All statuses' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All statuses</SelectItem>
                {HUB_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
            <Select
              value={filterFormValues.hubType}
              onValueChange={(value) =>
                onFilterFieldChange('hubType', value as 'ALL' | HubType)
              }
            >
              <SelectTrigger id='hub-filter-hub-type' className='w-full'>
                <SelectValue placeholder='All types' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All types</SelectItem>
                {HUB_TYPE_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-province'>Province</Label>
            <Select
              value={selectedFilterProvinceCode || 'ALL'}
              onValueChange={(value) => {
                const nextProvinceCode = value === 'ALL' ? '' : value;
                onFilterFieldChange('provinceCode', nextProvinceCode);
                onFilterFieldChange('wardCode', '');
              }}
            >
              <SelectTrigger id='hub-filter-province' className='w-full'>
                <SelectValue placeholder='All provinces' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All provinces</SelectItem>
                {provinceSelectOptions.map((province) => {
                  if (!province.provinceCode) {
                    return null;
                  }

                  return (
                    <SelectItem
                      key={province.provinceCode}
                      value={province.provinceCode}
                    >
                      {province.name} ({province.provinceCode})
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-ward'>Ward</Label>
            <Select
              value={selectedFilterWardCode || 'ALL'}
              onValueChange={(value) =>
                onFilterFieldChange('wardCode', value === 'ALL' ? '' : value)
              }
              disabled={!selectedFilterProvinceCode}
            >
              <SelectTrigger id='hub-filter-ward' className='w-full'>
                <SelectValue
                  placeholder={
                    selectedFilterProvinceCode
                      ? 'All wards'
                      : 'Select province first'
                  }
                />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All wards</SelectItem>
                {selectedFilterProvinceCode && isFetchingWardsForFilter ? (
                  <SelectItem value='__loading__' disabled>
                    Loading wards...
                  </SelectItem>
                ) : filterWardOptions.length > 0 ? (
                  filterWardOptions.map((ward) => {
                    if (!ward.wardCode) {
                      return null;
                    }

                    return (
                      <SelectItem key={ward.wardCode} value={ward.wardCode}>
                        {ward.name} ({ward.wardCode})
                      </SelectItem>
                    );
                  })
                ) : (
                  <SelectItem value='__empty__' disabled>
                    No wards available
                  </SelectItem>
                )}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-has-location'>Has location</Label>
            <Select
              value={filterFormValues.hasLocation}
              onValueChange={(value) =>
                onFilterFieldChange('hasLocation', value as HasLocationFilter)
              }
            >
              <SelectTrigger id='hub-filter-has-location' className='w-full'>
                <SelectValue placeholder='All records' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All records</SelectItem>
                <SelectItem value='YES'>Has location</SelectItem>
                <SelectItem value='NO'>No location</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-min-daily-capacity'>
              Min daily capacity
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
              Max daily capacity
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
            <Label htmlFor='hub-filter-min-current-load'>Min current load</Label>
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
            <Label htmlFor='hub-filter-max-current-load'>Max current load</Label>
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
