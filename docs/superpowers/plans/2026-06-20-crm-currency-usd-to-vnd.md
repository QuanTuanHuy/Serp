# CRM Currency Change from USD to VND Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modify the CRM module in `serp_web` to display and format all currency metrics in Vietnamese Dong (VND) instead of US Dollar (USD), using standard locale settings and compact notations.

**Architecture:** Update the centralized utility `formatCurrency` to use `vi-VN` locale and `VND` currency, supporting an optional `compact` parameter. Replace duplicate/inline formatters across cards, dashboard stats, pages, and charts to use the unified helper.

**Tech Stack:** React, Next.js, Lucide Icons, TypeScript

---

### Task 1: Update formatCurrency Utility

**Files:**
- Modify: `serp_web/src/modules/crm/utils/opportunityFormatters.ts`

- [ ] **Step 1: Write the updated utility code**
  Modify [opportunityFormatters.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/utils/opportunityFormatters.ts) to support both compact and full formats in `VND`.
  
  ```typescript
  export const formatCurrency = (value?: number, compact = false): string => {
    if (value === undefined) return 'Not available';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
      ...(compact ? { notation: 'compact', maximumFractionDigits: 1 } : {}),
    }).format(value);
  };
  ```

- [ ] **Step 2: Verify linting of utility file**
  Run: `npx eslint src/modules/crm/utils/opportunityFormatters.ts` in `serp_web/`
  Expected: No linting errors.

- [ ] **Step 3: Commit**
  Run:
  ```bash
  git add src/modules/crm/utils/opportunityFormatters.ts
  git commit -m "feat(crm): update formatCurrency utility for VND and compact mode"
  ```

---

### Task 2: Refactor Card Components

**Files:**
- Modify: `serp_web/src/modules/crm/components/cards/CustomerCard.tsx`
- Modify: `serp_web/src/modules/crm/components/cards/LeadCard.tsx`
- Modify: `serp_web/src/modules/crm/components/cards/OpportunityCard.tsx`

- [ ] **Step 1: Update CustomerCard**
  Modify [CustomerCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/cards/CustomerCard.tsx):
  - Remove the duplicate local `formatCurrency` function definitions (lines 70-74).
  - Add import of `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - Update usage at lines 290:
    ```typescript
    {formatCurrency(customer.totalValue || 0, true)}
    ```

- [ ] **Step 2: Update LeadCard**
  Modify [LeadCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/cards/LeadCard.tsx):
  - Remove local `formatCurrency` function definitions (lines 134-138).
  - Add import of `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - Update usage at lines 294 and 520:
    ```typescript
    {formatCurrency(lead.estimatedValue, true)}
    ```
    ```typescript
    {lead.estimatedValue ? formatCurrency(lead.estimatedValue, true) : '-'}
    ```

- [ ] **Step 3: Update OpportunityCard**
  Modify [OpportunityCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/cards/OpportunityCard.tsx):
  - Remove local `formatCurrency` function definitions (lines 98-102).
  - Add import of `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - Update usage at lines 249, 307, 426, 432:
    ```typescript
    {formatCurrency(displayValue, true)}
    ```

- [ ] **Step 4: Verify type checks for cards**
  Run: `npm run type-check` in `serp_web/`
  Expected: Command completes with no errors.

- [ ] **Step 5: Commit**
  Run:
  ```bash
  git add src/modules/crm/components/cards/CustomerCard.tsx src/modules/crm/components/cards/LeadCard.tsx src/modules/crm/components/cards/OpportunityCard.tsx
  git commit -m "refactor(crm): use centralized VND formatter in compact card components"
  ```

---

### Task 3: Refactor Dashboard & Opportunity Stats

**Files:**
- Modify: `serp_web/src/modules/crm/components/dashboard/PipelineFunnel.tsx`
- Modify: `serp_web/src/modules/crm/components/dashboard/StatsCard.tsx`
- Modify: `serp_web/src/modules/crm/components/opportunities/OpportunityStats.tsx`
- Modify: `serp_web/src/modules/crm/components/opportunities/OpportunityPipelineView.tsx`

- [ ] **Step 1: Update Dashboard PipelineFunnel**
  Modify [PipelineFunnel.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dashboard/PipelineFunnel.tsx):
  - Remove local `formatCurrency` function definitions (lines 79-86).
  - Import `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - Update usages (lines 194 and 223) to use `formatCurrency(stage.value, true)` and `formatCurrency(totalValue, true)`.

- [ ] **Step 2: Update StatsCard**
  Modify [StatsCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dashboard/StatsCard.tsx):
  - Import `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - Replace line 228:
    ```typescript
    value={formatCurrency(value)}
    ```

- [ ] **Step 3: Update OpportunityStats**
  Modify [OpportunityStats.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/opportunities/OpportunityStats.tsx):
  - Import `formatCurrency` from `../../utils` (line 6).
  - Replace occurrences of `$${Math.round(...).toLocaleString()}` at lines 25, 31, 43 with `formatCurrency(totalValue)`, `formatCurrency(weightedValue)`, and `formatCurrency(avgDealSize)`.

- [ ] **Step 4: Update OpportunityPipelineView**
  Modify [OpportunityPipelineView.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/opportunities/OpportunityPipelineView.tsx):
  - Add `formatCurrency` to existing imports from `../../utils` (line 13).
  - Replace line 68:
    ```typescript
    {formatCurrency(stageValues[stage as OpportunityStage], true)}
    ```

- [ ] **Step 5: Verify linting**
  Run: `npx eslint src/modules/crm/components/dashboard/PipelineFunnel.tsx src/modules/crm/components/dashboard/StatsCard.tsx src/modules/crm/components/opportunities/OpportunityStats.tsx src/modules/crm/components/opportunities/OpportunityPipelineView.tsx` in `serp_web/`
  Expected: No linting errors.

- [ ] **Step 6: Commit**
  Run:
  ```bash
  git add src/modules/crm/components/dashboard/PipelineFunnel.tsx src/modules/crm/components/dashboard/StatsCard.tsx src/modules/crm/components/opportunities/OpportunityStats.tsx src/modules/crm/components/opportunities/OpportunityPipelineView.tsx
  git commit -m "refactor(crm): update dashboard metrics and pipeline stage values to use VND formatter"
  ```

---

### Task 4: Refactor SalesChart Component

**Files:**
- Modify: `serp_web/src/modules/crm/components/analytics/SalesChart.tsx`

- [ ] **Step 1: Update SalesChart**
  Modify [SalesChart.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/analytics/SalesChart.tsx):
  - Import `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - In `CustomTooltip` (line 42), replace `$${entry.value?.toLocaleString()}` with `{formatCurrency(entry.value)}`.
  - In `YAxis` (lines 160 and 192), replace `tickFormatter={(value) => \`$\${value / 1000}K\`}` with `tickFormatter={(value) => formatCurrency(value, true)}`.

- [ ] **Step 2: Verify type checks for SalesChart**
  Run: `npm run type-check` in `serp_web/`
  Expected: No type issues.

- [ ] **Step 3: Commit**
  Run:
  ```bash
  git add src/modules/crm/components/analytics/SalesChart.tsx
  git commit -m "refactor(crm): update SalesChart YAxis ticks and tooltip to VND format"
  ```

---

### Task 5: Refactor List Pages

**Files:**
- Modify: `serp_web/src/modules/crm/pages/customers/CustomerListPage.tsx`
- Modify: `serp_web/src/modules/crm/pages/leads/LeadListPage.tsx`

- [ ] **Step 1: Update CustomerListPage**
  Modify [CustomerListPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/customers/CustomerListPage.tsx):
  - Import `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - Replace line 274:
    ```typescript
    value={formatCurrency(stats.totalValue)}
    ```

- [ ] **Step 2: Update LeadListPage**
  Modify [LeadListPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/leads/LeadListPage.tsx):
  - Import `formatCurrency` from `../../utils`:
    ```typescript
    import { formatCurrency } from '../../utils';
    ```
  - Replace line 573:
    ```typescript
    value={formatCurrency(stats.avgValue)}
    ```

- [ ] **Step 3: Verify all changes**
  Run: `npm run lint` and `npm run type-check` in `serp_web/`
  Expected: SUCCESS

- [ ] **Step 4: Commit**
  Run:
  ```bash
  git add src/modules/crm/pages/customers/CustomerListPage.tsx src/modules/crm/pages/leads/LeadListPage.tsx
  git commit -m "refactor(crm): replace hardcoded dollar list headers on Customer and Lead list pages"
  ```

---

### Task 6: Final Verification & Production Build

**Files:**
- None

- [ ] **Step 1: Run production build check**
  Run: `npm run build` in `serp_web/`
  Expected: Next.js compilation succeeds with zero errors.
