# CRM Currency Unification (USD to VND) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all hardcoded USD ($) currency indicators in the CRM module with formatted Vietnamese Dong (VNĐ) using the existing `formatCurrency` utility function.

**Architecture:** We will import and call `formatCurrency` in the forms, dialogs, cards, and analytics components that display opportunity and customer values.

**Tech Stack:** Next.js, React, TypeScript

---

### Task 1: Update Opportunity Form

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/OpportunityForm.tsx`

- [ ] **Step 1: Add import for `formatCurrency`**
  Add the import to `serp_web/src/modules/crm/components/forms/OpportunityForm.tsx` near existing imports:
  ```typescript
  import { formatCurrency } from '../../utils';
  ```

- [ ] **Step 2: Update Weighted Value rendering**
  Change line 413 of `serp_web/src/modules/crm/components/forms/OpportunityForm.tsx`:
  ```typescript
  // Target:
  <p className='text-xl font-semibold'>
    ${weightedValue.toLocaleString()}
  </p>

  // Replacement:
  <p className='text-xl font-semibold'>
    {formatCurrency(weightedValue)}
  </p>
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Command succeeds with no type errors.

- [ ] **Step 4: Commit**
  ```bash
  git add src/modules/crm/components/forms/OpportunityForm.tsx
  git commit -m "feat(crm): unify currency to VND in OpportunityForm"
  ```

---

### Task 2: Update Quick Add Opportunity Dialog

**Files:**
- Modify: `serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx`

- [ ] **Step 1: Add import for `formatCurrency`**
  Add the import to `serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx` near existing imports:
  ```typescript
  import { formatCurrency } from '../../utils';
  ```

- [ ] **Step 2: Update weighted value rendering text**
  Change line 322 of `serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx`:
  ```typescript
  // Target:
  <p className='text-xs text-muted-foreground'>
    Weighted value: ${weightedValue.toLocaleString()}
  </p>

  // Replacement:
  <p className='text-xs text-muted-foreground'>
    Weighted value: {formatCurrency(weightedValue)}
  </p>
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Command succeeds with no type errors.

- [ ] **Step 4: Commit**
  ```bash
  git add src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx
  git commit -m "feat(crm): unify currency to VND in QuickAddOpportunityDialog"
  ```

---

### Task 3: Update Customer/Opportunity Entity Cards

**Files:**
- Modify: `serp_web/src/modules/crm/components/shared/EntityCard.tsx`

- [ ] **Step 1: Add import for `formatCurrency`**
  Add the import to `serp_web/src/modules/crm/components/shared/EntityCard.tsx` near existing imports:
  ```typescript
  import { formatCurrency } from '../../utils';
  ```

- [ ] **Step 2: Update Customer total value rendering**
  Change line 135 of `serp_web/src/modules/crm/components/shared/EntityCard.tsx`:
  ```typescript
  // Target:
  <span className='text-xs text-gray-500'>
    ${customer.totalValue?.toLocaleString() || '0'}
  </span>

  // Replacement:
  <span className='text-xs text-gray-500'>
    {formatCurrency(customer.totalValue || 0)}
  </span>
  ```

- [ ] **Step 3: Update Opportunity value rendering**
  Change line 192 of `serp_web/src/modules/crm/components/shared/EntityCard.tsx`:
  ```typescript
  // Target:
  <span className='text-sm font-semibold text-green-600'>
    ${opportunity.value?.toLocaleString() || '0'}
  </span>

  // Replacement:
  <span className='text-sm font-semibold text-green-600'>
    {formatCurrency(opportunity.value || 0)}
  </span>
  ```

- [ ] **Step 4: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Command succeeds with no type errors.

- [ ] **Step 5: Commit**
  ```bash
  git add src/modules/crm/components/shared/EntityCard.tsx
  git commit -m "feat(crm): unify currency to VND in EntityCard"
  ```

---

### Task 4: Update Pipeline Chart

**Files:**
- Modify: `serp_web/src/modules/crm/components/analytics/PipelineChart.tsx`

- [ ] **Step 1: Add import for `formatCurrency`**
  Add the import to `serp_web/src/modules/crm/components/analytics/PipelineChart.tsx`:
  ```typescript
  import { formatCurrency } from '../../utils';
  ```

- [ ] **Step 2: Update tooltip value rendering**
  Change lines 60-66 of `serp_web/src/modules/crm/components/analytics/PipelineChart.tsx`:
  ```typescript
  // Target:
  {payload[0].payload.totalValue && (
    <p className='text-sm text-muted-foreground'>
      Value:{' '}
      <span className='font-medium text-emerald-600 dark:text-emerald-400'>
        ${payload[0].payload.totalValue.toLocaleString()}
      </span>
    </p>
  )}

  // Replacement:
  {payload[0].payload.totalValue && (
    <p className='text-sm text-muted-foreground'>
      Value:{' '}
      <span className='font-medium text-emerald-600 dark:text-emerald-400'>
        {formatCurrency(payload[0].payload.totalValue)}
      </span>
    </p>
  )}
  ```

- [ ] **Step 3: Update Summary Cards values**
  Change lines 203-224 of `serp_web/src/modules/crm/components/analytics/PipelineChart.tsx`:
  ```typescript
  // Target:
  <div className='text-center p-3 bg-muted/50 rounded-lg'>
    <p className='text-sm text-muted-foreground'>Total Value</p>
    <p className='text-xl font-bold text-emerald-600 dark:text-emerald-400'>
      $
      {data
        .reduce((sum, item) => sum + item.value, 0)
        .toLocaleString()}
    </p>
  </div>
  <div className='text-center p-3 bg-muted/50 rounded-lg col-span-2 md:col-span-1'>
    <p className='text-sm text-muted-foreground'>Avg Deal Size</p>
    <p className='text-xl font-bold text-primary'>
      $
      {(
        data.reduce((sum, item) => sum + item.value, 0) /
        Math.max(
          data.reduce((sum, item) => sum + item.count, 0),
          1
        )
      ).toLocaleString()}
    </p>
  </div>

  // Replacement:
  <div className='text-center p-3 bg-muted/50 rounded-lg'>
    <p className='text-sm text-muted-foreground'>Total Value</p>
    <p className='text-xl font-bold text-emerald-600 dark:text-emerald-400'>
      {formatCurrency(data.reduce((sum, item) => sum + item.value, 0))}
    </p>
  </div>
  <div className='text-center p-3 bg-muted/50 rounded-lg col-span-2 md:col-span-1'>
    <p className='text-sm text-muted-foreground'>Avg Deal Size</p>
    <p className='text-xl font-bold text-primary'>
      {formatCurrency(
        data.reduce((sum, item) => sum + item.value, 0) /
        Math.max(
          data.reduce((sum, item) => sum + item.count, 0),
          1
        )
      )}
    </p>
  </div>
  ```

- [ ] **Step 4: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Command succeeds with no type errors.

- [ ] **Step 5: Commit**
  ```bash
  git add src/modules/crm/components/analytics/PipelineChart.tsx
  git commit -m "feat(crm): unify currency to VND in PipelineChart"
  ```

---

### Task 5: Update Sales Chart

**Files:**
- Modify: `serp_web/src/modules/crm/components/analytics/SalesChart.tsx`

- [ ] **Step 1: Update Total Revenue rendering in SalesChart**
  Change lines 140-146 of `serp_web/src/modules/crm/components/analytics/SalesChart.tsx`:
  ```typescript
  // Target:
  <div className='text-right'>
    <p className='text-sm text-muted-foreground'>Total Revenue</p>
    <p className='text-xl font-bold text-emerald-600 dark:text-emerald-400'>
      $
      {data
        .reduce((sum, item) => sum + item.revenue, 0)
        .toLocaleString()}
    </p>
  </div>

  // Replacement:
  <div className='text-right'>
    <p className='text-sm text-muted-foreground'>Total Revenue</p>
    <p className='text-xl font-bold text-emerald-600 dark:text-emerald-400'>
      {formatCurrency(data.reduce((sum, item) => sum + item.revenue, 0))}
    </p>
  </div>
  ```

- [ ] **Step 2: Update Average Deal Size rendering in SalesChart**
  Change lines 228-239 of `serp_web/src/modules/crm/components/analytics/SalesChart.tsx`:
  ```typescript
  // Target:
  <div className='text-center p-3 bg-muted/50 rounded-lg'>
    <p className='text-sm text-muted-foreground'>Avg Deal Size</p>
    <p className='text-xl font-bold text-violet-600 dark:text-violet-400'>
      $
      {(
        data.reduce((sum, item) => sum + item.revenue, 0) /
        Math.max(
          data.reduce((sum, item) => sum + item.deals, 0),
          1
        )
      ).toLocaleString()}
    </p>
  </div>

  // Replacement:
  <div className='text-center p-3 bg-muted/50 rounded-lg'>
    <p className='text-sm text-muted-foreground'>Avg Deal Size</p>
    <p className='text-xl font-bold text-violet-600 dark:text-violet-400'>
      {formatCurrency(
        data.reduce((sum, item) => sum + item.revenue, 0) /
        Math.max(
          data.reduce((sum, item) => sum + item.deals, 0),
          1
        )
      )}
    </p>
  </div>
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run type-check` from `serp_web/`
  Expected: Command succeeds with no type errors.

- [ ] **Step 4: Commit**
  ```bash
  git add src/modules/crm/components/analytics/SalesChart.tsx
  git commit -m "feat(crm): unify currency to VND in SalesChart"
  ```
