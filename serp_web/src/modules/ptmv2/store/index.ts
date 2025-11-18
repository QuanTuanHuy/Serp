/**
 * PTM v2 - Store Configuration
 * 
 * @author QuanTuanHuy
 * @description Part of Serp Project - Redux store setup
 */

import { combineReducers } from '@reduxjs/toolkit';
import uiReducer from './uiSlice';

// PTM v2 reducer (UI state only, API handled by shared API slice)
export const ptmReducer = combineReducers({
  ui: uiReducer,
});

export type PTMState = ReturnType<typeof ptmReducer>;

// No separate middleware needed - using shared API middleware