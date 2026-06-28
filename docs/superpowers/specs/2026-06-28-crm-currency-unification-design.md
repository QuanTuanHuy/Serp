# Design Spec: CRM Currency Unification (USD to VND)

**Date:** 2026-06-28
**Author:** Antigravity (AI Coding Assistant)

## Goal
Unify the currency unit in the CRM module of the `serp_web` project from USD ($) to Vietnamese Dong (VNĐ / ₫).

## Proposed Changes
We will replace all hardcoded dollar signs ($) in the CRM module with the existing `formatCurrency` utility function. This utility uses standard Vietnamese locale settings to output formatted VND currency (e.g., `150.000.000 ₫`).

### Files Modified

#### 1. [OpportunityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/OpportunityForm.tsx)
- Import `formatCurrency` from `../../utils`.
- Change Weighted Value rendering from `${weightedValue.toLocaleString()}` to `{formatCurrency(weightedValue)}`.

#### 2. [QuickAddOpportunityDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx)
- Import `formatCurrency` from `../../utils`.
- Change Weighted Value rendering from `Weighted value: ${weightedValue.toLocaleString()}` to `Weighted value: {formatCurrency(weightedValue)}`.

#### 3. [EntityCard.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/shared/EntityCard.tsx)
- Import `formatCurrency` from `../../utils`.
- In `renderCustomerCard`, format `customer.totalValue` with `formatCurrency`.
- In `renderOpportunityCard`, format `opportunity.value` with `formatCurrency`.

#### 4. [PipelineChart.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/analytics/PipelineChart.tsx)
- Import `formatCurrency` from `../../utils`.
- In `CustomTooltip`, format `payload[0].payload.totalValue` with `formatCurrency`.
- In summary cards, format total value and average deal size with `formatCurrency`.

#### 5. [SalesChart.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/analytics/SalesChart.tsx)
- Replace hardcoded `$` rendering with `formatCurrency` for Total Revenue and Average Deal Size.

## Verification
- Build and run the app.
- Check the Create/Edit Opportunity page, Customer List page (EntityCards), Opportunity Kanban/Pipeline page, and the Analytics dashboard to verify all currency figures are formatted as `₫` (VND) instead of `$`.
