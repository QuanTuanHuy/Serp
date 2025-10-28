# Plan Modules Management Implementation

**Author:** QuanTuanHuy  
**Date:** October 28, 2025  
**Feature:** Subscription Plan Modules Management

## Overview

This implementation adds comprehensive module management functionality to subscription plans, allowing administrators to:

- View all modules in a plan
- Add modules to a plan with custom license types
- Remove modules from a plan
- Configure per-module user limits

## Architecture Changes

### 1. API Layer (`serp_web/src/modules/admin/services/plans/plansApi.ts`)

Added three new RTK Query endpoints:

```typescript
// Get all modules for a specific plan
getPlanModules: builder.query<PlanModule[], string>;

// Add a module to a plan
addModuleToPlan: builder.mutation<
  void,
  { planId: string; data: AddModuleToPlanRequest }
>;

// Remove a module from a plan
removeModuleFromPlan: builder.mutation<
  void,
  { planId: string; moduleId: number }
>;
```

**Cache Management:**

- Added `admin/PlanModule` tag type for fine-grained cache invalidation
- Automatically invalidates plan and plan-module caches on mutations

### 2. Type Definitions (`serp_web/src/modules/admin/types/plans.types.ts`)

Added comprehensive TypeScript types:

```typescript
// License types matching backend enum
type LicenseType =
  | 'FREE'
  | 'BASIC'
  | 'PROFESSIONAL'
  | 'ENTERPRISE'
  | 'TRIAL'
  | 'CUSTOM';

// Plan module representation
interface PlanModule {
  id: number;
  moduleId: number;
  moduleName: string;
  moduleCode: string;
  isIncluded: boolean;
  licenseType: LicenseType;
  maxUsersPerModule?: number;
}

// Request payload for adding modules
interface AddModuleToPlanRequest {
  moduleId: number;
  licenseType: LicenseType;
  isIncluded?: boolean;
  maxUsersPerModule?: number;
}
```

### 3. Hook Enhancement (`serp_web/src/modules/admin/hooks/usePlans.ts`)

Extended `usePlans` hook with module management:

**New State:**

```typescript
- modulesDialogOpen: boolean
- modulesDialogPlanId: string | null
- planModules: PlanModule[]
- isLoadingModules: boolean
- isAddingModule: boolean
- isRemovingModule: boolean
```

**New Actions:**

```typescript
- openModulesDialog(planId): void
- closeModulesDialog(): void
- addModuleToPlan(planId, data): Promise<void>
- removeModuleFromPlan(planId, moduleId): Promise<void>
```

### 4. UI Component (`serp_web/src/modules/admin/components/plans/PlanModulesDialog.tsx`)

Created a modern, feature-rich dialog component with:

#### Features:

1. **Module List View**
   - Displays all modules currently in the plan
   - Shows license type with color-coded badges
   - Displays max users per module (if set)
   - Active/inactive status indicators
   - Search/filter functionality

2. **Add Module Form**
   - Dropdown to select from available modules (not yet in plan)
   - License type selector with visual badges
   - Optional max users per module field
   - Real-time validation

3. **Remove Module**
   - One-click removal with confirmation
   - Automatic cache refresh

#### Visual Design:

- **License Type Badges:** Color-coded for easy identification
  - FREE: Green
  - BASIC: Blue
  - PROFESSIONAL: Purple
  - ENTERPRISE: Orange
  - TRIAL: Yellow
  - CUSTOM: Pink

- **Module Cards:** Clean card-based layout with:
  - Module name and code
  - License type badge
  - User limit indicator
  - Quick delete action

- **Responsive Layout:** Mobile-friendly with proper overflow handling

### 5. Page Integration (`serp_web/src/app/admin/plans/page.tsx`)

Updated plans page with:

#### Grid View:

- Added "Modules" button in card actions
- Positioned prominently alongside "Edit" button

#### Table View:

- Added "Manage Modules" as first action in dropdown menu
- Maintains consistency with other actions

## Backend Integration

### Endpoints Used:

```
GET    /api/v1/subscription-plans/:planId/modules       # Get plan modules
POST   /api/v1/subscription-plans/:planId/modules       # Add module to plan
DELETE /api/v1/subscription-plans/:planId/modules/:moduleId  # Remove module
```

### Backend Structure Reference:

- **Entity:** `SubscriptionPlanModuleEntity.java`
- **Controller:** `SubscriptionPlanController.java`
- **Request DTO:** `AddModuleToPlanRequest.java`
- **Response DTO:** `SubscriptionPlanDetailResponse.PlanModuleResponse`
- **Enum:** `LicenseType.java`

## User Workflow

### Viewing Modules

1. Navigate to Admin → Plans
2. Click "Modules" button on any plan card (grid view)
3. Or click "Manage Modules" in action menu (table view)
4. Dialog opens showing all modules in the plan

### Adding a Module

1. Click "Add Module" button in the dialog
2. Select module from dropdown (shows only available modules)
3. Choose license type
4. Optionally set max users per module
5. Click "Add Module"
6. Module appears in the list immediately

### Removing a Module

1. Click trash icon on any module card
2. Confirm deletion in popup
3. Module is removed instantly

## Technical Highlights

### Smart Module Filtering

```typescript
const availableModules = useMemo(() => {
  const planModuleIds = new Set(planModules.map((pm) => pm.moduleId));
  return allModules.filter((m) => !planModuleIds.has(m.id));
}, [allModules, planModules]);
```

### Optimistic UI Updates

- Uses RTK Query cache invalidation
- Immediate UI feedback on mutations
- Automatic refetch on success

### Error Handling

- Graceful error messages via notification system
- Form validation before submission
- Confirmation dialogs for destructive actions

## Files Modified/Created

### Created:

- `serp_web/src/modules/admin/components/plans/PlanModulesDialog.tsx` (400+ lines)

### Modified:

- `serp_web/src/lib/store/api/apiSlice.ts` - Added tag type
- `serp_web/src/modules/admin/types/plans.types.ts` - Added types
- `serp_web/src/modules/admin/types/index.ts` - Exported new types
- `serp_web/src/modules/admin/services/plans/plansApi.ts` - Added endpoints
- `serp_web/src/modules/admin/hooks/usePlans.ts` - Extended hook
- `serp_web/src/modules/admin/components/plans/index.ts` - Exported component
- `serp_web/src/modules/admin/index.ts` - Exported useModules
- `serp_web/src/app/admin/plans/page.tsx` - Integrated UI

## Testing Checklist

- [ ] View modules for a plan
- [ ] Add a module with FREE license
- [ ] Add a module with PROFESSIONAL license
- [ ] Set max users per module
- [ ] Remove a module from plan
- [ ] Search/filter modules in list
- [ ] Handle empty module list
- [ ] Handle no available modules to add
- [ ] Test responsive layout on mobile
- [ ] Verify cache invalidation works

## Future Enhancements

1. **Bulk Operations**
   - Add multiple modules at once
   - Bulk remove modules
   - Import/export module configurations

2. **Module Details**
   - View full module information
   - Show module dependencies
   - Display module pricing

3. **Advanced Filtering**
   - Filter by license type
   - Filter by active/inactive
   - Sort by module name/code

4. **Analytics**
   - Track module usage per plan
   - Most popular modules
   - License type distribution

## Performance Considerations

- **Lazy Loading:** Modules only fetched when dialog opens
- **Memoization:** Filtered lists use `useMemo`
- **Conditional Queries:** Skip queries when dialog is closed
- **Optimized Renders:** Component properly memoizes expensive calculations

## Accessibility

- Proper ARIA labels on all interactive elements
- Keyboard navigation support
- Focus management in dialog
- Screen reader friendly status indicators

## Notes

- All components follow Clean Architecture principles
- Consistent with existing admin module patterns
- Fully typed with TypeScript
- Uses Shadcn UI components for consistency
- Follows the project's file header convention
