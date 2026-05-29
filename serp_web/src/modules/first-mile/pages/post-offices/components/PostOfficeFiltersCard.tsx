/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Post office filters (basic/advanced)
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
            <Select
              value={filterFormValues.status}
              onValueChange={(value) =>
                onFilterFieldChange(
                  'status',
                  value as PostOfficeFilterFormState['status']
                )
              }
            >
              <SelectTrigger id='post-office-filter-status' className='w-full'>
                <SelectValue placeholder='All statuses' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>All statuses</SelectItem>
                {POST_OFFICE_STATUS_OPTIONS.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='post-office-filter-province'>Province</Label>
            <Select
              value={selectedFilterProvinceCode || 'ALL'}
              onValueChange={(value) => {
                const nextProvinceCode = value === 'ALL' ? '' : value;
                onFilterFieldChange('provinceCode', nextProvinceCode);
                onFilterFieldChange('wardCode', '');
              }}
            >
              <SelectTrigger id='post-office-filter-province' className='w-full'>
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
            <Label htmlFor='post-office-filter-ward'>Ward</Label>
            <Select
              value={selectedFilterWardCode || 'ALL'}
              onValueChange={(value) =>
                onFilterFieldChange('wardCode', value === 'ALL' ? '' : value)
              }
              disabled={!selectedFilterProvinceCode}
            >
              <SelectTrigger id='post-office-filter-ward' className='w-full'>
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
            <Label htmlFor='post-office-filter-has-location'>Has location</Label>
            <Select
              value={filterFormValues.hasLocation}
              onValueChange={(value) =>
                onFilterFieldChange('hasLocation', value as HasLocationFilter)
              }
            >
              <SelectTrigger
                id='post-office-filter-has-location'
                className='w-full'
              >
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
            <Label htmlFor='post-office-filter-hub-id'>Hub ID</Label>
            <Select
              value={filterFormValues.hubId || 'ALL'}
              onValueChange={(value) =>
                onFilterFieldChange('hubId', value === 'ALL' ? '' : value)
              }
            >
              <SelectTrigger id='post-office-filter-hub-id' className='w-full'>
                <SelectValue placeholder='Any hub' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='ALL'>Any hub</SelectItem>
                {hubOptions.map((hub) => (
                  <SelectItem key={hub.id} value={String(hub.id)}>
                    {hub.code} - {hub.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
            <Label htmlFor='post-office-filter-min-priority'>Min priority</Label>
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
            <Label htmlFor='post-office-filter-max-priority'>Max priority</Label>
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
