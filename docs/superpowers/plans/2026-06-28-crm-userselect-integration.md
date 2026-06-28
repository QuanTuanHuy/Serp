# CRM User Select Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a reusable `CRMUserSelect` component and integrate it across CRM forms and dialogs to replace manual User ID text inputs.

**Architecture:** Implement a single `CRMUserSelect` shared component that hooks up Redux selector and user queries, exports it in shared components, and applies it to the forms/dialogs (using `react-hook-form` Controller or local state).

**Tech Stack:** React, Next.js, Redux, TypeScript, react-hook-form

---

### Task 1: Create CRMUserSelect Component

**Files:**
- Create: `serp_web/src/modules/crm/components/shared/CRMUserSelect.tsx`
- Modify: `serp_web/src/modules/crm/components/shared/index.ts`

- [ ] **Step 1: Create CRMUserSelect component file**
  Create `serp_web/src/modules/crm/components/shared/CRMUserSelect.tsx` with this content:
  ```typescript
  /**
   * Author: QuanTuanHuy
   * Description: Part of Serp Project - CRM shared user selector component
   */

  'use client';

  import React from 'react';
  import { selectOrganizationId } from '@/modules/account/store';
  import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
  import { useAppSelector } from '@/shared/hooks';
  import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
  } from '@/shared/components/ui';
  import { cn } from '@/shared/utils';

  interface CRMUserSelectProps {
    value?: string;
    onChange: (value: string) => void;
    disabled?: boolean;
    className?: string;
    placeholder?: string;
    id?: string;
    fallbackUserName?: string;
  }

  export const CRMUserSelect: React.FC<CRMUserSelectProps> = ({
    value = '',
    onChange,
    disabled = false,
    className,
    placeholder = 'Unassigned',
    id,
    fallbackUserName,
  }) => {
    const organizationId = useAppSelector(selectOrganizationId);
    const { data: orgUsersResponse, isLoading } = useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
      },
      { skip: !organizationId }
    );

    const orgUsers = orgUsersResponse?.data?.items ?? [];
    const valString = value?.trim() ?? '';

    const formatOrgUserLabel = (u: (typeof orgUsers)[number]) => {
      const name = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
      return name || u.email || `User #${u.id}`;
    };

    const showOrphanAssignee =
      Boolean(valString) &&
      !orgUsers.some((u) => String(u.id) === valString);
    const orphanAssigneeLabel = fallbackUserName || `User #${valString}`;

    return (
      <div className='w-full'>
        <Select
          disabled={disabled || isLoading || !organizationId}
          value={valString ? valString : '_none'}
          onValueChange={(v) => onChange(v === '_none' ? '' : v)}
        >
          <SelectTrigger id={id} className={className}>
            <SelectValue
              placeholder={isLoading ? 'Loading users…' : placeholder}
            />
          </SelectTrigger>
          <SelectContent position='popper' className='max-h-60'>
            <SelectItem value='_none'>{placeholder}</SelectItem>
            {showOrphanAssignee && (
              <SelectItem value={valString}>
                {orphanAssigneeLabel}
              </SelectItem>
            )}
            {orgUsers.map((u) => (
              <SelectItem key={u.id} value={String(u.id)}>
                {formatOrgUserLabel(u)}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {!organizationId && (
          <p className='text-[10px] text-muted-foreground mt-1'>
            Your profile does not include an organization yet; assignees cannot be loaded.
          </p>
        )}
      </div>
    );
  };

  export default CRMUserSelect;
  ```

- [ ] **Step 2: Export CRMUserSelect in shared/index.ts**
  Modify `serp_web/src/modules/crm/components/shared/index.ts`:
  ```typescript
  // Target:
  export { StatusBadge } from './StatusBadge';
  export { PriorityBadge } from './PriorityBadge';
  export { CRMDatePicker, CRMDateRangePicker, CRMDateTimePicker } from './CRMDatePicker';

  // Replacement:
  export { StatusBadge } from './StatusBadge';
  export { PriorityBadge } from './PriorityBadge';
  export { CRMDatePicker, CRMDateRangePicker, CRMDateTimePicker } from './CRMDatePicker';
  export { CRMUserSelect } from './CRMUserSelect';
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Success

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/shared/CRMUserSelect.tsx serp_web/src/modules/crm/components/shared/index.ts
  git commit -m "feat(crm): create and export CRMUserSelect component"
  ```

---

### Task 2: Integrate with Lead Form

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/LeadForm.tsx`

- [ ] **Step 1: Import CRMUserSelect**
  Add import for `CRMUserSelect` near the top of `serp_web/src/modules/crm/components/forms/LeadForm.tsx`:
  ```typescript
  // Target:
  import { cn } from '@/shared/utils';
  import { CRMDatePicker } from '../shared';

  // Replacement:
  import { cn } from '@/shared/utils';
  import { CRMDatePicker, CRMUserSelect } from '../shared';
  ```

- [ ] **Step 2: Replace Assigned User ID input with CRMUserSelect**
  Change lines 327-334 of `serp_web/src/modules/crm/components/forms/LeadForm.tsx`:
  ```typescript
  // Target:
              <div className='space-y-2'>
                <Label htmlFor='assignedTo'>Assigned User ID</Label>
                <Input
                  id='assignedTo'
                  value={formData.assignedTo}
                  onChange={(e) => updateField('assignedTo', e.target.value)}
                  disabled={isLoading}
                />
              </div>

  // Replacement:
              <div className='space-y-2'>
                <Label htmlFor='assignedTo'>Assigned To</Label>
                <CRMUserSelect
                  id='assignedTo'
                  value={formData.assignedTo}
                  onChange={(v) => updateField('assignedTo', v)}
                  fallbackUserName={lead?.assignedToName}
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
  git commit -m "feat(crm): integrate CRMUserSelect in LeadForm"
  ```

---

### Task 3: Integrate with Activity Form

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/ActivityForm.tsx`

- [ ] **Step 1: Import CRMUserSelect**
  Add `CRMUserSelect` to the shared components imports in `serp_web/src/modules/crm/components/forms/ActivityForm.tsx`:
  ```typescript
  // Target:
  import { cn } from '@/shared/utils';
  import { CRMDatePicker } from '../shared';

  // Replacement:
  import { cn } from '@/shared/utils';
  import { CRMDatePicker, CRMUserSelect } from '../shared';
  ```

- [ ] **Step 2: Replace assignedTo Input with CRMUserSelect wrapped in Controller**
  Change lines 333-352 of `serp_web/src/modules/crm/components/forms/ActivityForm.tsx`:
  ```typescript
  // Target:
                <div className='space-y-2'>
                  <Label htmlFor='assignedTo'>Assigned To</Label>
                  <Input
                    id='assignedTo'
                    type='number'
                    {...register('assignedTo')}
                    placeholder='Enter user ID'
                    className={errors.assignedTo ? 'border-destructive' : ''}
                    disabled={isLoading}
                  />
                  {errors.assignedTo ? (
                    <p className='text-sm text-destructive'>
                      {errors.assignedTo.message}
                    </p>
                  ) : (
                    <p className='text-xs text-muted-foreground'>
                      Current assignee: {activity.assignedToName || 'Unassigned'}
                    </p>
                  )}
                </div>

  // Replacement:
                <div className='space-y-2'>
                  <Label htmlFor='assignedTo'>Assigned To</Label>
                  <Controller
                    name='assignedTo'
                    control={control}
                    render={({ field }) => (
                      <CRMUserSelect
                        id='assignedTo'
                        value={field.value}
                        onChange={field.onChange}
                        fallbackUserName={activity.assignedToName}
                        disabled={isLoading}
                        className={errors.assignedTo ? 'border-destructive' : ''}
                      />
                    )}
                  />
                  {errors.assignedTo && (
                    <p className='text-sm text-destructive'>
                      {errors.assignedTo.message}
                    </p>
                  )}
                </div>
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check`
  Expected: Success

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/forms/ActivityForm.tsx
  git commit -m "feat(crm): integrate CRMUserSelect in ActivityForm"
  ```

---

### Task 4: Integrate with Quick Add Dialogs

**Files:**
- Modify: `serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx`
- Modify: `serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx`

- [ ] **Step 1: Integrate with QuickAddActivityDialog**
  Open `serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx`.
  Add `CRMUserSelect` to the shared component imports:
  ```typescript
  // Target:
  import { CRMDatePicker } from '../shared';

  // Replacement:
  import { CRMDatePicker, CRMUserSelect } from '../shared';
  ```
  Replace `<Input type="number" value={formData.assignedTo} ... />` on lines 450-459:
  ```typescript
  // Target:
            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <Users className='h-4 w-4 text-muted-foreground' />
                Assigned To (Optional)
              </Label>
              <Input
                type='number'
                value={formData.assignedTo}
                onChange={(e) => handleChange('assignedTo', e.target.value)}
                placeholder='Enter user ID'
              />
              <p className='text-xs text-muted-foreground'>
                Leave empty to assign to yourself
              </p>
            </div>

  // Replacement:
            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <Users className='h-4 w-4 text-muted-foreground' />
                Assigned To
              </Label>
              <CRMUserSelect
                value={formData.assignedTo}
                onChange={(v) => handleChange('assignedTo', v)}
                disabled={isLoading}
              />
            </div>
  ```

- [ ] **Step 2: Integrate with QuickAddOpportunityDialog**
  Open `serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx`.
  Add `CRMUserSelect` to imports:
  ```typescript
  // Target:
  import { CRMDatePicker } from '../shared';

  // Replacement:
  import { CRMDatePicker, CRMUserSelect } from '../shared';
  ```
  Replace `<Input id='assignedTo' ... />` on lines 273-286:
  ```typescript
  // Target:
              <Label htmlFor='assignedTo' className='flex items-center gap-2'>
                <User className='h-4 w-4 text-muted-foreground' />
                Assigned To
              </Label>
              <Input
                id='assignedTo'
                value={formData.assignedTo || ''}
                onChange={(e) => handleChange('assignedTo', e.target.value)}
                placeholder='Optional assignee user ID'
                disabled={isLoading}
              />

  // Replacement:
              <Label htmlFor='assignedTo' className='flex items-center gap-2'>
                <User className='h-4 w-4 text-muted-foreground' />
                Assigned To
              </Label>
              <CRMUserSelect
                id='assignedTo'
                value={formData.assignedTo || ''}
                onChange={(v) => handleChange('assignedTo', v)}
                disabled={isLoading}
              />
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check`
  Expected: Success

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx
  git commit -m "feat(crm): integrate CRMUserSelect in QuickAdd dialogs"
  ```

---

### Task 5: Refactor Opportunity Form

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/OpportunityForm.tsx`

- [ ] **Step 1: Import CRMUserSelect**
  Add `CRMUserSelect` to shared components imports:
  ```typescript
  // Target:
  import { CRMDatePicker } from '../shared';

  // Replacement:
  import { CRMDatePicker, CRMUserSelect } from '../shared';
  ```

- [ ] **Step 2: Remove redundant queries and users loading logic**
  Find and remove the following lines from `serp_web/src/modules/crm/components/forms/OpportunityForm.tsx` (lines 121-136 approx):
  ```typescript
  // Target:
    const { data: orgUsersResponse, isLoading: isOrgUsersLoading } =
      useGetOrganizationUsersQuery(
        {
          organizationId: organizationId as number,
          page: 0,
          pageSize: 100,
          status: 'ACTIVE',
        },
        { skip: !organizationId }
      );
    const orgUsers = orgUsersResponse?.data?.items ?? [];

    const formatOrgUserLabel = (u: (typeof orgUsers)[number]) => {
      const name = [u.firstName, u.lastName].filter(Boolean).join(' ').trim();
      return name || u.email || `User #${u.id}`;
    };
  ```
  (Note: Keep `const organizationId = useAppSelector(selectOrganizationId);` as it is used elsewhere if necessary, or clean it up if it's unused. In `OpportunityForm.tsx`, it's only used for those lines and the fallback text, which is now handled by `CRMUserSelect`!)
  Also remove these lines:
  ```typescript
    const showOrphanAssignee =
      Boolean(assignedToValue) &&
      !orgUsers.some((u) => String(u.id) === assignedToValue);
    const orphanAssigneeLabel =
      opportunity?.assignedTo === assignedToValue && opportunity.assignedToName
        ? opportunity.assignedToName
        : `User #${assignedToValue}`;
  ```

- [ ] **Step 3: Replace the custom Select logic with CRMUserSelect wrapped in Controller**
  Change lines 330-366 approx:
  ```typescript
  // Target:
                <Label htmlFor='assignedTo'>Assigned To</Label>
                <Select
                  disabled={isLoading || isOrgUsersLoading || !organizationId}
                  value={assignedToValue ? assignedToValue : '_none'}
                  onValueChange={(v) =>
                    setValue('assignedTo', v === '_none' ? '' : v, {
                      shouldValidate: true,
                    })
                  }
                >
                  <SelectTrigger id='assignedTo'>
                    <SelectValue
                      placeholder={
                        isOrgUsersLoading ? 'Loading users…' : 'Unassigned'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent position='popper' className='max-h-60'>
                    <SelectItem value='_none'>Unassigned</SelectItem>
                    {showOrphanAssignee && (
                      <SelectItem value={assignedToValue}>
                        {orphanAssigneeLabel}
                      </SelectItem>
                    )}
                    {orgUsers.map((u) => (
                      <SelectItem key={u.id} value={String(u.id)}>
                        {formatOrgUserLabel(u)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {!organizationId && (
                  <p className='text-xs text-muted-foreground'>
                    Your profile does not include an organization yet; assignees
                    cannot be loaded.
                  </p>
                )}

  // Replacement:
                <Label htmlFor='assignedTo'>Assigned To</Label>
                <Controller
                  name='assignedTo'
                  control={control}
                  render={({ field }) => (
                    <CRMUserSelect
                      id='assignedTo'
                      value={field.value}
                      onChange={field.onChange}
                      fallbackUserName={opportunity?.assignedToName}
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
  git add serp_web/src/modules/crm/components/forms/OpportunityForm.tsx
  git commit -m "refactor(crm): replace custom select with CRMUserSelect in OpportunityForm"
  ```
