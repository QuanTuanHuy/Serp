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
import type { Province, Ward } from '../../../../types';
import { HUB_STATUS_OPTIONS } from '../hubForm';
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
    { value: 'ALL', label: 'Tất cả trạng thái' },
    ...HUB_STATUS_OPTIONS,
  ];
  const provinceOptions = [
    { value: 'ALL', label: 'Tất cả tỉnh/thành phố' },
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
    { value: 'ALL', label: 'Tất cả phường/xã' },
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
    { value: 'ALL', label: 'Tất cả bản ghi' },
    { value: 'YES', label: 'Đã có tọa độ' },
    { value: 'NO', label: 'Chưa có tọa độ' },
  ];

  return (
    <TmsListFilterPanel
      title='Tìm kiếm và bộ lọc'
      description='Tìm kiếm nhanh theo từ khóa, trạng thái và các tham số lọc của API hub.'
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
            <Label htmlFor='hub-filter-keyword'>Từ khóa</Label>
            <div className='relative'>
              <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
              <Input
                id='hub-filter-keyword'
                className='pl-10'
                value={filterFormValues.keyword}
                onChange={(event) =>
                  onFilterFieldChange('keyword', event.target.value)
                }
                placeholder='Mã, tên, địa chỉ...'
              />
            </div>
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-status'>Trạng thái</Label>
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
              placeholder='Tất cả trạng thái'
              emptyText='Không tìm thấy trạng thái'
            />
          </div>
        </>
      }
      advancedFilters={
        <>
          <div className='space-y-2'>
            <Label htmlFor='hub-filter-code'>Mã hub</Label>
            <Input
              id='hub-filter-code'
              value={filterFormValues.code}
              onChange={(event) =>
                onFilterFieldChange('code', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-name'>Tên hub</Label>
            <Input
              id='hub-filter-name'
              value={filterFormValues.name}
              onChange={(event) =>
                onFilterFieldChange('name', event.target.value)
              }
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-province'>Tỉnh/Thành phố</Label>
            <TmsCombobox
              id='hub-filter-province'
              value={selectedFilterProvinceCode || 'ALL'}
              onValueChange={(value) => {
                const nextProvinceCode = value === 'ALL' ? '' : value;
                onFilterFieldChange('provinceCode', nextProvinceCode);
                onFilterFieldChange('wardCode', '');
              }}
              options={provinceOptions}
              placeholder='Tất cả tỉnh/thành phố'
              emptyText='Không tìm thấy tỉnh/thành phố'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-ward'>Phường/Xã</Label>
            <TmsCombobox
              id='hub-filter-ward'
              value={selectedFilterWardCode || 'ALL'}
              onValueChange={(value) =>
                onFilterFieldChange('wardCode', value === 'ALL' ? '' : value)
              }
              options={wardOptions}
              placeholder={
                selectedFilterProvinceCode
                  ? 'Tất cả phường/xã'
                  : 'Chọn tỉnh/thành phố trước'
              }
              emptyText={
                isFetchingWardsForFilter
                  ? 'Đang tải phường/xã...'
                  : 'Không có phường/xã'
              }
              disabled={!selectedFilterProvinceCode}
              loading={isFetchingWardsForFilter}
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-has-location'>Tọa độ</Label>
            <TmsCombobox
              id='hub-filter-has-location'
              value={filterFormValues.hasLocation}
              onValueChange={(value) =>
                onFilterFieldChange('hasLocation', value as HasLocationFilter)
              }
              options={hasLocationOptions}
              placeholder='Tất cả bản ghi'
              emptyText='Không tìm thấy tùy chọn'
            />
          </div>

          <div className='space-y-2'>
            <Label htmlFor='hub-filter-min-daily-capacity'>
              Sức chứa đơn tối thiểu
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
              Sức chứa đơn tối đa
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
              Tải hub tối thiểu
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
              Tải hub tối đa
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
