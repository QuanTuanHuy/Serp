# Spec: CRM Currency Change from USD to VND

- **Author**: Antigravity
- **Date**: 2026-06-20
- **Status**: Approved

## 1. Goal

Review and modify the `crm` module in `serp_web` to convert all currency display and formatting from US Dollar ($ / USD) to Vietnamese Dong (₫ / VND).

## 2. Approved Options

* **Compact Formatting**: Use JavaScript's built-in `Intl.NumberFormat` with `notation: 'compact'` for compact numbers in cards and dashboard metrics (resulting in native Vietnamese forms like `10 Tr ₫` for millions and `1,2 T ₫` for billions).
* **Icons**: Retain the Lucide `DollarSign` icon in the user interface as it is a widely recognized symbol for financial metrics/opportunities in general UI design.

## 3. Proposed Changes

We will modify the formatting logic in the following components and pages:

### 3.1. Utility Formatter
* **File**: [opportunityFormatters.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/utils/opportunityFormatters.ts)
  * Update `formatCurrency` to use `vi-VN` locale and `VND` currency.
  * Add a `compact?: boolean` parameter to switch between full currency formatting and compact currency formatting.

### 3.2. Cards (Compact Views)
* **Files**:
  * [CustomerCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/cards/CustomerCard.tsx)
  * [LeadCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/cards/LeadCard.tsx)
  * [OpportunityCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/cards/OpportunityCard.tsx)
* **Changes**:
  * Remove local duplicate `formatCurrency` functions.
  * Import `formatCurrency` from `../../utils`.
  * Update render calls to `formatCurrency(value, true)`.

### 3.3. Dashboard and Stats Components
* **Files**:
  * [PipelineFunnel.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dashboard/PipelineFunnel.tsx) (dashboard view)
  * [StatsCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dashboard/StatsCard.tsx)
  * [OpportunityStats.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/opportunities/OpportunityStats.tsx)
  * [OpportunityPipelineView.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/opportunities/OpportunityPipelineView.tsx)
* **Changes**:
  * Clean up and align formatting to use `formatCurrency(value)` or `formatCurrency(value, true)` instead of hardcoded dollar templates.

### 3.4. Analytics Components
* **File**: [SalesChart.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/analytics/SalesChart.tsx)
  * Import `formatCurrency` from `../../utils`.
  * Update tick formatter to `(value) => formatCurrency(value, true)`.
  * Update `CustomTooltip` value output to `{formatCurrency(entry.value)}`.

### 3.5. List Pages
* **Files**:
  * [CustomerListPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/customers/CustomerListPage.tsx)
  * [LeadListPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/leads/LeadListPage.tsx)
* **Changes**:
  * Import `formatCurrency` from `../../utils`.
  * Replace `$${value.toLocaleString()}` with `formatCurrency(value)`.

## 4. Verification Plan

* Run ESLint and TypeScript check (`npm run lint` and `npm run type-check` in `serp_web/`) to verify there are no broken imports or type mismatches.
* Build the application locally (`npm run build` from `serp_web/`) to ensure complete builds pass.
