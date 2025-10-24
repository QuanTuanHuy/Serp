/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin store barrel exports and reducer
 */

import { combineReducers } from '@reduxjs/toolkit';
import { plansReducer } from './plans/plansSlice';

export const adminReducer = combineReducers({
  plans: plansReducer,
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
