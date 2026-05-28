/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Province and ward selectors for billing
 */

import React from 'react';
import {
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
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
          <Select value={provinceCode} onValueChange={onProvinceChange}>
            <SelectTrigger id={provinceId}>
              <SelectValue
                placeholder={
                  isFetchingProvinces ? 'Loading provinces...' : 'Select province'
                }
              />
            </SelectTrigger>
            <SelectContent>
              {provinceOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className='space-y-2'>
          <Label htmlFor={wardId} className='text-xs'>
            Ward
          </Label>
          <Select
            value={wardCode}
            onValueChange={onWardChange}
            disabled={!hasProvince}
          >
            <SelectTrigger id={wardId}>
              <SelectValue
                placeholder={
                  !hasProvince
                    ? 'Select province first'
                    : isFetchingWards
                      ? 'Loading wards...'
                      : 'Select ward'
                }
              />
            </SelectTrigger>
            <SelectContent>
              {wardOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
    </div>
  );
};
