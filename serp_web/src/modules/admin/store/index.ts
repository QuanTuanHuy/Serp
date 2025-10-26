/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin store barrel exports and reducer
 */

import { combineReducers } from '@reduxjs/toolkit';
import { plansReducer } from './plans/plansSlice';
import { organizationsReducer } from './organizations/organizationsSlice';
import { modulesReducer } from './modules/modulesSlice';
import { rolesReducer } from './roles/rolesSlice';
import { subscriptionsReducer } from './subscriptions/subscriptionsSlice';

export const adminReducer = combineReducers({
  plans: plansReducer,
  organizations: organizationsReducer,
  modules: modulesReducer,
  roles: rolesReducer,
  subscriptions: subscriptionsReducer,
});

export {
  setViewMode,
  setDialogOpen,
  setSelectedPlanId,
  clearSelectedPlan,
  selectPlansUiState,
  selectPlansViewMode,
  selectPlansDialogOpen,
  selectSelectedPlanId,
} from './plans/plansSlice';

export {
  setFilters as setOrganizationsFilters,
  setSearch as setOrganizationsSearch,
  setStatus as setOrganizationsStatus,
  setType as setOrganizationsType,
  setPage as setOrganizationsPage,
  setPageSize as setOrganizationsPageSize,
  setSort as setOrganizationsSort,
  selectOrganizationsFilters,
} from './organizations/organizationsSlice';

export {
  setModulesDialogOpen,
  setSelectedModuleId,
  clearSelectedModule,
  selectModulesUi,
  selectModulesDialogOpen,
  selectSelectedModuleId,
  setModulesFilters,
  setModulesSearch,
  setModulesStatus,
  setModulesType,
  selectModulesFilters,
} from './modules/modulesSlice';

export {
  setFilters as setRolesFilters,
  setSearch as setRolesSearch,
  setScope as setRolesScope,
  setRoleType as setRolesRoleType,
  setOrganizationId as setRolesOrganizationId,
  setModuleId as setRolesModuleId,
  setIsDefault as setRolesIsDefault,
  setPage as setRolesPage,
  setPageSize as setRolesPageSize,
  setSort as setRolesSort,
  setSelectedRoleId,
  clearSelectedRole,
  setDialogOpen as setRolesDialogOpen,
  setViewMode as setRolesViewMode,
  selectRolesFilters,
  selectRolesUiState,
  selectSelectedRoleId,
  selectRolesDialogOpen,
  selectRolesViewMode,
} from './roles/rolesSlice';

export {
  setSubscriptionsFilters,
  setSubscriptionsStatus,
  setSubscriptionsOrganizationId,
  setSubscriptionsPlanId,
  setSubscriptionsBillingCycle,
  setSubscriptionsPage,
  setSubscriptionsPageSize,
  setSubscriptionsSort,
  setSelectedSubscriptionId,
  clearSelectedSubscription,
  selectSubscriptionsFilters,
  selectSelectedSubscriptionId,
} from './subscriptions/subscriptionsSlice';
