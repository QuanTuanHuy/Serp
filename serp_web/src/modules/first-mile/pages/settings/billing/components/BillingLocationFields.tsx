/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Province and ward selectors for billing
 */

import React from 'react';
import { TmsCombobox } from '@/modules/first-mile/components';
import { Label } from '@/shared/components/ui';
import type { BillingSelectOption } from '../billingPageModels';

interface BillingLocationFieldsProps {
  label: string;
  provinceId: string;
  wardId: string;
  provinceCode: string;
  wardCode: string;
  provinceOptions: BillingSelectOption[];
  wardOptions: BillingSelectOption[];
  isFetchingProvinces: boolean;
  isFetchingWards: boolean;
  onProvinceChange: (provinceCode: string) => void;
  onWardChange: (wardCode: string) => void;
}

export const BillingLocationFields: React.FC<BillingLocationFieldsProps> = ({
  label,
  provinceId,
  wardId,
  provinceCode,
  wardCode,
  provinceOptions,
  wardOptions,
  isFetchingProvinces,
  isFetchingWards,
  onProvinceChange,
  onWardChange,
}) => {
  const hasProvince = Boolean(provinceCode.trim());

  return (
    <div className='space-y-2'>
      <Label>{label}</Label>
      <div className='grid gap-3 md:grid-cols-2'>
        <div className='space-y-2'>
          <Label htmlFor={provinceId} className='text-xs'>
            Province
          </Label>
          <TmsCombobox
            id={provinceId}
            value={provinceCode}
            onValueChange={onProvinceChange}
            options={provinceOptions}
            placeholder={
              isFetchingProvinces ? 'Loading provinces...' : 'Select province'
            }
            emptyText='No provinces found'
            loading={isFetchingProvinces}
          />
        </div>

        <div className='space-y-2'>
          <Label htmlFor={wardId} className='text-xs'>
            Ward
          </Label>
          <TmsCombobox
            id={wardId}
            value={wardCode}
            onValueChange={onWardChange}
            options={wardOptions}
            placeholder={
              !hasProvince
                ? 'Select province first'
                : isFetchingWards
                  ? 'Loading wards...'
                  : 'Select ward'
            }
            emptyText='No wards found'
            disabled={!hasProvince}
            loading={isFetchingWards}
          />
        </div>
      </div>
    </div>
  );
};
