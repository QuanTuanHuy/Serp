# CRM Date Picker Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Clone `PMDatePicker.tsx` as `CRMDatePicker.tsx` and apply it to all date inputs in the CRM module.

**Architecture:** Create CRM local date helpers, create `CRMDatePicker` by cloning `PMDatePicker`, export them properly, and then refactor forms (using `react-hook-form` Controller or local state) and list pages to use the new date picker.

**Tech Stack:** React, Next.js, TypeScript, react-hook-form, lucide-react, date-fns

---

### Task 1: Create CRM Date Helpers

**Files:**
- Create: `serp_web/src/modules/crm/utils/date.ts`
- Modify: `serp_web/src/modules/crm/utils/index.ts`

- [ ] **Step 1: Create local date utility**
  Create `serp_web/src/modules/crm/utils/date.ts` with the following content:
  ```typescript
  // Author: QuanTuanHuy
  // Description: Part of Serp Project - CRM local date helpers

  const DATE_ONLY_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

  export function parseLocalDateValue(
    value?: number | string | Date | null
  ): Date | undefined {
    if (value === undefined || value === null || value === '') {
      return undefined;
    }

    if (value instanceof Date) {
      return Number.isNaN(value.getTime()) ? undefined : value;
    }

    if (typeof value === 'number') {
      const date = new Date(value);
      return Number.isNaN(date.getTime()) ? undefined : date;
    }

    if (DATE_ONLY_PATTERN.test(value)) {
      const [year, month, day] = value.split('-').map((part) => Number(part));
      if (!year || !month || !day) {
        return undefined;
      }

      const date = new Date(year, month - 1, day);
      return Number.isNaN(date.getTime()) ? undefined : date;
    }

    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? undefined : parsed;
  }

  export function toLocalDateInputValue(
    value?: number | string | Date | null
  ): string {
    const date = parseLocalDateValue(value);
    if (!date) {
      return '';
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
  ```

- [ ] **Step 2: Export date utilities**
  Modify `serp_web/src/modules/crm/utils/index.ts`:
  ```typescript
  // Target:
  export * from './opportunityFormatters';

  // Replacement:
  export * from './opportunityFormatters';
  export * from './date';
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Success

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/utils/date.ts serp_web/src/modules/crm/utils/index.ts
  git commit -m "feat(crm): add local date helpers for CRM"
  ```

---

### Task 2: Create CRMDatePicker Component

**Files:**
- Create: `serp_web/src/modules/crm/components/shared/CRMDatePicker.tsx`
- Modify: `serp_web/src/modules/crm/components/shared/index.ts`

- [ ] **Step 1: Clone and rename date picker component**
  Create `serp_web/src/modules/crm/components/shared/CRMDatePicker.tsx` based on `PMDatePicker.tsx` but renamed and with updated imports:
  ```typescript
  /**
   * Author: QuanTuanHuy
   * Description: Part of Serp Project - CRM shared date picker
   */

  'use client';

  import { forwardRef, useState, type ComponentPropsWithoutRef } from 'react';
  import { CalendarDays, Clock, X } from 'lucide-react';
  import { format } from 'date-fns';
  import { Button } from '@/shared/components/ui/button';
  import { Calendar } from '@/shared/components/ui/calendar';
  import {
    Popover,
    PopoverContent,
    PopoverTrigger,
  } from '@/shared/components/ui/popover';
  import { cn } from '@/shared/utils';
  import { parseLocalDateValue } from '../../utils/date';

  interface CRMDatePickerProps
    extends Omit<
      ComponentPropsWithoutRef<typeof Button>,
      'className' | 'children' | 'onChange' | 'type' | 'disabled' | 'value'
    > {
    value?: number | string | Date | null;
    onChange: (value?: Date) => void;
    placeholder?: string;
    disabled?: boolean;
    showClear?: boolean;
    align?: 'start' | 'center' | 'end';
    className?: string;
    buttonClassName?: string;
    contentClassName?: string;
    clearLabel?: string;
  }

  interface CRMDateRangePickerProps {
    from?: number | string | Date | null;
    to?: number | string | Date | null;
    onFromChange: (value?: Date) => void;
    onToChange: (value?: Date) => void;
    fromLabel?: string;
    toLabel?: string;
    fromPlaceholder?: string;
    toPlaceholder?: string;
    disabled?: boolean;
    className?: string;
  }

  interface CRMDateTimePickerProps
    extends Omit<
      ComponentPropsWithoutRef<typeof Button>,
      'className' | 'children' | 'onChange' | 'type' | 'disabled' | 'value'
    > {
    value?: number | string | Date | null;
    onChange: (value?: Date) => void;
    placeholder?: string;
    disabled?: boolean;
    showClear?: boolean;
    align?: 'start' | 'center' | 'end';
    className?: string;
    buttonClassName?: string;
    contentClassName?: string;
    clearLabel?: string;
    defaultTime?: string;
  }

  export const CRMDatePicker = forwardRef<HTMLButtonElement, CRMDatePickerProps>(
    function CRMDatePicker(
      {
        value,
        onChange,
        placeholder = 'Pick a date',
        disabled,
        showClear = true,
        align = 'start',
        className,
        buttonClassName,
        contentClassName,
        clearLabel = 'Clear date',
        ...buttonProps
      },
      ref
    ) {
      const [open, setOpen] = useState(false);
      const selectedDate = parseLocalDateValue(value);
      const hasValue = Boolean(selectedDate);

      return (
        <div className={cn('flex min-w-0 items-center gap-2', className)}>
          <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
              <Button
                ref={ref}
                type='button'
                variant='outline'
                disabled={disabled}
                {...buttonProps}
                className={cn(
                  'min-w-0 flex-1 justify-start gap-2 text-left font-normal',
                  !hasValue && 'text-muted-foreground',
                  buttonClassName
                )}
              >
                <CalendarDays className='h-4 w-4 shrink-0' />
                <span className='min-w-0 flex-1 truncate'>
                  {hasValue ? format(selectedDate!, 'PPP') : placeholder}
                </span>
              </Button>
            </PopoverTrigger>
            <PopoverContent
              align={align}
              className={cn('w-auto p-0', contentClassName)}
            >
              <Calendar
                mode='single'
                selected={selectedDate}
                onSelect={(date) => {
                  onChange(date ?? undefined);
                  setOpen(false);
                }}
                initialFocus
              />
            </PopoverContent>
          </Popover>

          {showClear && hasValue ? (
            <Button
              type='button'
              variant='ghost'
              size='icon'
              className='h-10 w-10 shrink-0'
              aria-label={clearLabel}
              onClick={() => onChange(undefined)}
              disabled={disabled}
            >
              <X className='h-4 w-4' />
            </Button>
          ) : null}
        </div>
      );
    }
  );

  export function CRMDateRangePicker({
    from,
    to,
    onFromChange,
    onToChange,
    fromLabel = 'From',
    toLabel = 'To',
    fromPlaceholder = 'Start date',
    toPlaceholder = 'End date',
    disabled,
    className,
  }: CRMDateRangePickerProps) {
    return (
      <div className={cn('grid gap-2 sm:grid-cols-2', className)}>
        <label className='space-y-1'>
          <span className='text-xs font-medium text-muted-foreground'>
            {fromLabel}
          </span>
          <CRMDatePicker
            value={from}
            onChange={onFromChange}
            placeholder={fromPlaceholder}
            disabled={disabled}
            showClear
            buttonClassName='flex-1'
          />
        </label>
        <label className='space-y-1'>
          <span className='text-xs font-medium text-muted-foreground'>
            {toLabel}
          </span>
          <CRMDatePicker
            value={to}
            onChange={onToChange}
            placeholder={toPlaceholder}
            disabled={disabled}
            showClear
            buttonClassName='flex-1'
          />
        </label>
      </div>
    );
  }

  export const CRMDateTimePicker = forwardRef<
    HTMLButtonElement,
    CRMDateTimePickerProps
  >(function CRMDateTimePicker(
    {
      value,
      onChange,
      placeholder = 'Pick date and time',
      disabled,
      showClear = true,
      align = 'start',
      className,
      buttonClassName,
      contentClassName,
      clearLabel = 'Clear date and time',
      defaultTime = '09:00',
      ...buttonProps
    },
    ref
  ) {
    const [open, setOpen] = useState(false);
    const selectedDate = parseLocalDateValue(value);
    const hasValue = Boolean(selectedDate);
    const timeValue = selectedDate
      ? toTimeInputValue(selectedDate)
      : normalizeTimeValue(defaultTime);

    const handleDateSelect = (date?: Date) => {
      if (!date) {
        onChange(undefined);
        return;
      }

      onChange(mergeDateAndTime(date, timeValue));
    };

    const handleTimeChange = (time: string) => {
      const normalizedTime = normalizeTimeValue(time);
      const baseDate = selectedDate ?? new Date();
      onChange(mergeDateAndTime(baseDate, normalizedTime));
    };

    return (
      <div className={cn('flex min-w-0 items-center gap-2', className)}>
        <Popover open={open} onOpenChange={setOpen}>
          <PopoverTrigger asChild>
            <Button
              ref={ref}
              type='button'
              variant='outline'
              disabled={disabled}
              {...buttonProps}
              className={cn(
                'min-w-0 flex-1 justify-start gap-2 text-left font-normal',
                !hasValue && 'text-muted-foreground',
                buttonClassName
              )}
            >
              <CalendarDays className='h-4 w-4 shrink-0' />
              <span className='min-w-0 flex-1 truncate'>
                {hasValue ? format(selectedDate!, 'PP p') : placeholder}
              </span>
            </Button>
          </PopoverTrigger>
          <PopoverContent
            align={align}
            className={cn('w-auto p-0', contentClassName)}
          >
            <Calendar
              mode='single'
              selected={selectedDate}
              onSelect={handleDateSelect}
              initialFocus
            />
            <div className='flex items-center gap-2 border-t p-3'>
              <Clock className='h-4 w-4 text-muted-foreground' />
              <input
                type='time'
                value={timeValue}
                onChange={(event) => handleTimeChange(event.target.value)}
                disabled={disabled}
                className='h-9 rounded-md border border-input bg-background px-3 text-sm outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50'
              />
            </div>
          </PopoverContent>
        </Popover>

        {showClear && hasValue ? (
          <Button
            type='button'
            variant='ghost'
            size='icon'
            className='h-10 w-10 shrink-0'
            aria-label={clearLabel}
            onClick={() => onChange(undefined)}
            disabled={disabled}
          >
            <X className='h-4 w-4' />
          </Button>
        ) : null}
      </div>
    );
  });

  function toTimeInputValue(date: Date): string {
    return `${String(date.getHours()).padStart(2, '0')}:${String(
      date.getMinutes()
    ).padStart(2, '0')}`;
  }

  // Fallback pattern matching
  function normalizeTimeValue(value: string): string {
    return /^\d{2}:\d{2}$/.test(value) ? value : '09:00';
  }

  function mergeDateAndTime(date: Date, time: string): Date {
    const [hours, minutes] = normalizeTimeValue(time)
      .split(':')
      .map((part) => Number(part));

    return new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      hours,
      minutes,
      0,
      0
    );
  }
  ```

- [ ] **Step 2: Export CRMDatePicker from shared**
  Modify `serp_web/src/modules/crm/components/shared/index.ts`:
  ```typescript
  // Target:
  export { StatusBadge } from './StatusBadge';

  // Replacement:
  export { StatusBadge } from './StatusBadge';
  export { CRMDatePicker, CRMDateRangePicker, CRMDateTimePicker } from './CRMDatePicker';
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Success

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/shared/CRMDatePicker.tsx serp_web/src/modules/crm/components/shared/index.ts
  git commit -m "feat(crm): create and export CRMDatePicker component"
  ```

---

### Task 3: Integrate with Opportunity Form

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/OpportunityForm.tsx`

- [ ] **Step 1: Import Controller, CRMDatePicker and toLocalDateInputValue**
  Add the imports at the top of `serp_web/src/modules/crm/components/forms/OpportunityForm.tsx`:
  ```typescript
  // Target:
  import { useForm } from 'react-hook-form';
  import { zodResolver } from '@hookform/resolvers/zod';

  // Replacement:
  import { useForm, Controller } from 'react-hook-form';
  import { zodResolver } from '@hookform/resolvers/zod';
  ```
  And add the components imports:
  ```typescript
  // Target:
    SelectTrigger,
    SelectValue,
  } from '@/shared/components/ui';
  import { cn } from '@/shared/utils';
  import { useGetAccountsQuery, useGetLeadsQuery } from '../../api/crmApi';

  // Replacement:
    SelectTrigger,
    SelectValue,
  } from '@/shared/components/ui';
  import { cn } from '@/shared/utils';
  import { useGetAccountsQuery, useGetLeadsQuery } from '../../api/crmApi';
  import { CRMDatePicker } from '../shared';
  import { toLocalDateInputValue } from '../../utils';
  ```

- [ ] **Step 2: Destructure `control` from `useForm`**
  Modify lines 154-164:
  ```typescript
  // Target:
    const {
      register,
      handleSubmit,
      watch,
      reset,
      formState: { errors, isSubmitting },
      setValue,
    } = useForm<OpportunityFormData>({

  // Replacement:
    const {
      register,
      handleSubmit,
      control,
      watch,
      reset,
      formState: { errors, isSubmitting },
      setValue,
    } = useForm<OpportunityFormData>({
  ```

- [ ] **Step 3: Replace expectedCloseDate input field with CRMDatePicker wrapped in Controller**
  Change lines 388-402:
  ```typescript
  // Target:
                <Label htmlFor='expectedCloseDate'>Expected Close Date *</Label>
                <Input
                  id='expectedCloseDate'
                  type='date'
                  {...register('expectedCloseDate')}
                  className={
                    errors.expectedCloseDate ? 'border-destructive' : ''
                  }
                  disabled={isLoading}
                />
                {errors.expectedCloseDate && (
                  <p className='text-sm text-destructive'>
                    {errors.expectedCloseDate.message}
                  </p>
                )}

  // Replacement:
                <Label htmlFor='expectedCloseDate'>Expected Close Date *</Label>
                <Controller
                  name='expectedCloseDate'
                  control={control}
                  render={({ field }) => (
                    <CRMDatePicker
                      id='expectedCloseDate'
                      value={field.value}
                      onChange={(date) =>
                        field.onChange(date ? toLocalDateInputValue(date) : '')
                      }
                      disabled={isLoading}
                      className={errors.expectedCloseDate ? 'border-destructive' : ''}
                    />
                  )}
                />
                {errors.expectedCloseDate && (
                  <p className='text-sm text-destructive'>
                    {errors.expectedCloseDate.message}
                  </p>
                )}
  ```

- [ ] **Step 4: Verify build**
  Run: `npm run type-check`
  Expected: Success

- [ ] **Step 5: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/forms/OpportunityForm.tsx
  git commit -m "feat(crm): integrate CRMDatePicker in OpportunityForm"
  ```

---

### Task 4: Integrate with Activity Form

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/ActivityForm.tsx`

- [ ] **Step 1: Import Controller, CRMDatePicker and toLocalDateInputValue**
  Modify imports:
  ```typescript
  // Target:
  import { useForm } from 'react-hook-form';
  import { zodResolver } from '@hookform/resolvers/zod';

  // Replacement:
  import { useForm, Controller } from 'react-hook-form';
  import { zodResolver } from '@hookform/resolvers/zod';
  ```
  And:
  ```typescript
  // Target:
    SelectTrigger,
    SelectValue,
  } from '@/shared/components/ui';
  import { cn } from '@/shared/utils';
  import { useGetAccountsQuery, useGetContactsQuery } from '../../api/crmApi';

  // Replacement:
    SelectTrigger,
    SelectValue,
  } from '@/shared/components/ui';
  import { cn } from '@/shared/utils';
  import { useGetAccountsQuery, useGetContactsQuery } from '../../api/crmApi';
  import { CRMDatePicker } from '../shared';
  import { toLocalDateInputValue } from '../../utils';
  ```

- [ ] **Step 2: Destructure `control` from `useForm`**
  ```typescript
  // Target:
    const {
      register,
      handleSubmit,
      setValue,
      watch,
      formState: { errors, isSubmitting },
    } = useForm<ActivityFormData>({

  // Replacement:
    const {
      register,
      handleSubmit,
      control,
      setValue,
      watch,
      formState: { errors, isSubmitting },
    } = useForm<ActivityFormData>({
  ```

- [ ] **Step 3: Replace scheduledDate input field with CRMDatePicker wrapped in Controller**
  Change lines 263-270:
  ```typescript
  // Target:
                <Label htmlFor='scheduledDate'>Date</Label>
                <Input
                  id='scheduledDate'
                  type='date'
                  {...register('scheduledDate')}
                  disabled={isLoading}
                />

  // Replacement:
                <Label htmlFor='scheduledDate'>Date</Label>
                <Controller
                  name='scheduledDate'
                  control={control}
                  render={({ field }) => (
                    <CRMDatePicker
                      id='scheduledDate'
                      value={field.value}
                      onChange={(date) =>
                        field.onChange(date ? toLocalDateInputValue(date) : '')
                      }
                      disabled={isLoading}
                    />
                  )}
                />
  ```

- [ ] **Step 4: Verify build**
  Run: `npm run type-check`
  Expected: Success

- [ ] **Step 5: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/forms/ActivityForm.tsx
  git commit -m "feat(crm): integrate CRMDatePicker in ActivityForm"
  ```

---

### Task 5: Integrate with Lead Form

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/LeadForm.tsx`

- [ ] **Step 1: Import CRMDatePicker and toLocalDateInputValue**
  Add imports to `serp_web/src/modules/crm/components/forms/LeadForm.tsx`:
  ```typescript
  // Target:
  import { cn } from '@/shared/utils';
  import type {

  // Replacement:
  import { cn } from '@/shared/utils';
  import { CRMDatePicker } from '../shared';
  import { toLocalDateInputValue } from '../../utils';
  import type {
  ```

- [ ] **Step 2: Replace followUpDate input field with CRMDatePicker**
  Change lines 336-344:
  ```typescript
  // Target:
              <div className='space-y-2'>
                <Label htmlFor='followUpDate'>Follow-up Date</Label>
                <Input
                  id='followUpDate'
                  type='date'
                  value={formData.followUpDate}
                  onChange={(e) => updateField('followUpDate', e.target.value)}
                  disabled={isLoading}
                />
              </div>

  // Replacement:
              <div className='space-y-2'>
                <Label htmlFor='followUpDate'>Follow-up Date</Label>
                <CRMDatePicker
                  id='followUpDate'
                  value={formData.followUpDate}
                  onChange={(date) =>
                    updateField('followUpDate', date ? toLocalDateInputValue(date) : '')
                  }
                  disabled={isLoading}
                />
              </div>
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check`
  Expected: Success

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/forms/LeadForm.tsx
  git commit -m "feat(crm): integrate CRMDatePicker in LeadForm"
  ```

---

### Task 6: Integrate with Quick Add Dialogs

**Files:**
- Modify: `serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx`
- Modify: `serp_web/src/modules/crm/components/dialogs/QuickAddLeadDialog.tsx`
- Modify: `serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx`

- [ ] **Step 1: Integrate with QuickAddActivityDialog**
  Open `serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx`, import `CRMDatePicker` and `toLocalDateInputValue`:
  ```typescript
  import { CRMDatePicker } from '../shared';
  import { toLocalDateInputValue } from '../../utils';
  ```
  Replace `<Input id='scheduledDate' type='date' ... />` with `<CRMDatePicker>`:
  ```typescript
  // Target:
                <Input
                  id='scheduledDate'
                  type='date'
                  value={formData.scheduledDate}
                  onChange={(e) => handleChange('scheduledDate', e.target.value)}
                />

  // Replacement:
                <CRMDatePicker
                  id='scheduledDate'
                  value={formData.scheduledDate}
                  onChange={(date) =>
                    handleChange('scheduledDate', date ? toLocalDateInputValue(date) : '')
                  }
                  disabled={isLoading}
                />
  ```

- [ ] **Step 2: Integrate with QuickAddLeadDialog**
  Open `serp_web/src/modules/crm/components/dialogs/QuickAddLeadDialog.tsx`, import `CRMDatePicker` and `toLocalDateInputValue`:
  ```typescript
  import { CRMDatePicker } from '../shared';
  import { toLocalDateInputValue } from '../../utils';
  ```
  Replace `<Input id='quick-followUpDate' type='date' ... />` with `<CRMDatePicker>`:
  ```typescript
  // Target:
                <Input
                  id='quick-followUpDate'
                  type='date'
                  value={formData.followUpDate}
                  onChange={(e) =>
                    setFormData((prev) => ({
                      ...prev,
                      followUpDate: e.target.value,
                    }))
                  }
                />

  // Replacement:
                <CRMDatePicker
                  id='quick-followUpDate'
                  value={formData.followUpDate}
                  onChange={(date) =>
                    setFormData((prev) => ({
                      ...prev,
                      followUpDate: date ? toLocalDateInputValue(date) : '',
                    }))
                  }
                  disabled={isLoading}
                />
  ```

- [ ] **Step 3: Integrate with QuickAddOpportunityDialog**
  Open `serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx`, import `toLocalDateInputValue`:
  (Note: `CRMDatePicker` is already imported or can be imported):
  ```typescript
  import { CRMDatePicker } from '../shared';
  import { toLocalDateInputValue } from '../../utils';
  ```
  Replace `<Input id='expectedCloseDate' type='date' ... />` with `<CRMDatePicker>`:
  ```typescript
  // Target:
              <Input
                id='expectedCloseDate'
                type='date'
                value={formData.expectedCloseDate}
                onChange={(e) =>
                  handleChange('expectedCloseDate', e.target.value)
                }
                className={cn(errors.expectedCloseDate && 'border-red-500')}
                disabled={isLoading}
              />

  // Replacement:
              <CRMDatePicker
                id='expectedCloseDate'
                value={formData.expectedCloseDate}
                onChange={(date) =>
                  handleChange('expectedCloseDate', date ? toLocalDateInputValue(date) : '')
                }
                className={cn(errors.expectedCloseDate && 'border-red-500')}
                disabled={isLoading}
              />
  ```

- [ ] **Step 4: Verify build**
  Run: `npm run type-check`
  Expected: Success

- [ ] **Step 5: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx serp_web/src/modules/crm/components/dialogs/QuickAddLeadDialog.tsx serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx
  git commit -m "feat(crm): integrate CRMDatePicker in QuickAdd dialogs"
  ```

---

### Task 7: Integrate with Activity List Page Filters

**Files:**
- Modify: `serp_web/src/modules/crm/pages/activities/ActivityListPage.tsx`

- [ ] **Step 1: Import CRMDatePicker and toLocalDateInputValue**
  Add imports to `serp_web/src/modules/crm/pages/activities/ActivityListPage.tsx`:
  ```typescript
  import { CRMDatePicker } from '../../components/shared';
  import { toLocalDateInputValue } from '../../utils';
  ```

- [ ] **Step 2: Replace Due Date From and Due Date To inputs**
  Change lines 496-520:
  ```typescript
  // Target:
                  <input
                    type='date'
                    value={dueDateFrom}
                    onChange={(e) => {
                      setDueDateFrom(e.target.value);
                      setCurrentPage(1);
                    }}
                    className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                  />
                </div>

                <div>
                  <label className='text-sm font-medium mb-1.5 block'>
                    Due Date To
                  </label>
                  <input
                    type='date'
                    value={dueDateTo}
                    onChange={(e) => {
                      setDueDateTo(e.target.value);
                      setCurrentPage(1);
                    }}
                    className='w-full px-3 py-2 border border-border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-ring'
                  />

  // Replacement:
                  <CRMDatePicker
                    value={dueDateFrom}
                    onChange={(date) => {
                      setDueDateFrom(date ? toLocalDateInputValue(date) : '');
                      setCurrentPage(1);
                    }}
                  />
                </div>

                <div>
                  <label className='text-sm font-medium mb-1.5 block'>
                    Due Date To
                  </label>
                  <CRMDatePicker
                    value={dueDateTo}
                    onChange={(date) => {
                      setDueDateTo(date ? toLocalDateInputValue(date) : '');
                      setCurrentPage(1);
                    }}
                  />
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check`
  Expected: Success

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/pages/activities/ActivityListPage.tsx
  git commit -m "feat(crm): integrate CRMDatePicker in ActivityListPage filters"
  ```
