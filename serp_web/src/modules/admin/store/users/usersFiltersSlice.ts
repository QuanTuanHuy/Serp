/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin Users filters slice
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store/store';
import type { UserFilters } from '../../types';

const initialState: UserFilters = {
  page: 0,
  pageSize: 10,
  sortBy: 'id',
  sortDir: 'DESC',
};

const usersFiltersSlice = createSlice({
  name: 'adminUsersFilters',
  initialState,
  reducers: {
    setUsersFilters(state, action: PayloadAction<Partial<UserFilters>>) {
      Object.assign(state, action.payload);
    },
    setUsersSearch(state, action: PayloadAction<string | undefined>) {
      state.search = action.payload;
      state.page = 0;
    },
    setUsersStatus(state, action: PayloadAction<UserFilters['status']>) {
      state.status = action.payload;
      state.page = 0;
    },
    setUsersOrganizationId(state, action: PayloadAction<number | undefined>) {
      state.organizationId = action.payload;
      state.page = 0;
    },
    setUsersPage(state, action: PayloadAction<number>) {
      state.page = action.payload;
    },
    setUsersPageSize(state, action: PayloadAction<number>) {
      state.pageSize = action.payload;
      state.page = 0;
    },
    setUsersSort(
      state,
      action: PayloadAction<{ sortBy: string; sortDir: 'ASC' | 'DESC' }>
    ) {
      state.sortBy = action.payload.sortBy;
      state.sortDir = action.payload.sortDir;
    },
    resetUsersFilters() {
      return initialState;
    },
  },
});

export const usersFiltersReducer = usersFiltersSlice.reducer;
export const {
  setUsersFilters,
  setUsersSearch,
  setUsersStatus,
  setUsersOrganizationId,
  setUsersPage,
  setUsersPageSize,
  setUsersSort,
  resetUsersFilters,
} = usersFiltersSlice.actions;

export const selectUsersFilters = (state: RootState) =>
  state.admin.usersFilters;
