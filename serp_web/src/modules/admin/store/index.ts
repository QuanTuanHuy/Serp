/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin store barrel exports and reducer
 */

import { combineReducers } from '@reduxjs/toolkit';
import { plansReducer } from './plans/plansSlice';
import { organizationsReducer } from './organizations/organizationsSlice';

export const adminReducer = combineReducers({
  plans: plansReducer,
  organizations: organizationsReducer,
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
