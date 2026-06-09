/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard filters
 */

import { RefreshCw } from 'lucide-react';
import type React from 'react';

import {
  Button,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';

import type {
  DashboardDatePreset,
  DashboardOption,
  DashboardSelectValue,
} from '../dashboardPageModels';
import {
  DATE_PRESET_OPTIONS,
  GRANULARITY_OPTIONS,
  SERVICE_TYPE_OPTIONS,
} from '../dashboardPageModels';
import type { TmsDashboardGranularity } from '@/modules/first-mile/types';

interface DashboardFiltersProps {
  datePreset: DashboardDatePreset;
  fromDate: string;
  toDate: string;
  granularity: TmsDashboardGranularity;
  selectedHubId: DashboardSelectValue;
  selectedPostOfficeCode: DashboardSelectValue;
  selectedServiceType: DashboardSelectValue;
  hubOptions: DashboardOption[];
  postOfficeOptions: DashboardOption[];
  canUseHubFilter: boolean;
  canUsePostOfficeFilter: boolean;
  isRefreshing: boolean;
  onDatePresetChange: (value: DashboardDatePreset) => void;
  onFromDateChange: (value: string) => void;
  onToDateChange: (value: string) => void;
  onGranularityChange: (value: TmsDashboardGranularity) => void;
  onHubChange: (value: DashboardSelectValue) => void;
  onPostOfficeChange: (value: DashboardSelectValue) => void;
  onServiceTypeChange: (value: DashboardSelectValue) => void;
  onRefresh: () => void;
}

export function DashboardFilters({
  datePreset,
  fromDate,
  toDate,
  granularity,
  selectedHubId,
  selectedPostOfficeCode,
  selectedServiceType,
  hubOptions,
  postOfficeOptions,
  canUseHubFilter,
  canUsePostOfficeFilter,
  isRefreshing,
  onDatePresetChange,
  onFromDateChange,
  onToDateChange,
  onGranularityChange,
  onHubChange,
  onPostOfficeChange,
  onServiceTypeChange,
  onRefresh,
}: DashboardFiltersProps) {
  return (
    <div className='grid gap-3 lg:grid-cols-[repeat(6,minmax(0,1fr))_auto]'>
      <Field label='Date range'>
        <Select
          value={datePreset}
          onValueChange={(value) =>
            onDatePresetChange(value as DashboardDatePreset)
          }
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {DATE_PRESET_OPTIONS.map((option) => (
              <SelectItem key={option.value} value={option.value}>
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </Field>

      <Field label='From'>
        <Input
          type='date'
          value={fromDate}
          onChange={(event) => onFromDateChange(event.target.value)}
        />
      </Field>

      <Field label='To'>
        <Input
          type='date'
          value={toDate}
          onChange={(event) => onToDateChange(event.target.value)}
        />
      </Field>

      <Field label='Granularity'>
        <Select
          value={granularity}
          onValueChange={(value) =>
            onGranularityChange(value as TmsDashboardGranularity)
          }
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {GRANULARITY_OPTIONS.map((option) => (
              <SelectItem key={option.value} value={option.value}>
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </Field>

      {canUseHubFilter && (
        <Field label='Hub'>
          <Select value={selectedHubId} onValueChange={onHubChange}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {hubOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </Field>
      )}

      {canUsePostOfficeFilter && (
        <Field label='Post office'>
          <Select
            value={selectedPostOfficeCode}
            onValueChange={onPostOfficeChange}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {postOfficeOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </Field>
      )}

      <Field label='Service'>
        <Select value={selectedServiceType} onValueChange={onServiceTypeChange}>
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {SERVICE_TYPE_OPTIONS.map((option) => (
              <SelectItem key={option.value} value={option.value}>
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </Field>

      <div className='flex items-end'>
        <Button
          variant='outline'
          size='icon'
          className='h-10 w-10'
          onClick={onRefresh}
          disabled={isRefreshing}
          title='Refresh dashboard'
        >
          <RefreshCw
            className={isRefreshing ? 'h-4 w-4 animate-spin' : 'h-4 w-4'}
          />
        </Button>
      </div>
    </div>
  );
}

function Field({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className='space-y-1.5'>
      <Label className='text-xs text-muted-foreground'>{label}</Label>
      {children}
    </div>
  );
}
