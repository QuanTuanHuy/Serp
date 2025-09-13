/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';

// Simplified AuthState - chỉ chứa authentication data
interface AuthState {
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  token: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'account/auth',
  initialState,
  reducers: {
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },

    setCredentials: (
      state,
      action: PayloadAction<{
        token: string;
        refreshToken: string;
      }>
    ) => {
      const { token, refreshToken } = action.payload;
      state.token = token;
      state.refreshToken = refreshToken;
      state.isAuthenticated = true;
      state.error = null;
    },

    setTokens: (
      state,
      action: PayloadAction<{
        token: string;
        refreshToken: string;
      }>
    ) => {
      const { token, refreshToken } = action.payload;
      state.token = token;
      state.refreshToken = refreshToken;
      state.isAuthenticated = true;
    },

    clearAuth: (state) => {
      state.token = null;
      state.refreshToken = null;
      state.isAuthenticated = false;
      state.error = null;
      state.isLoading = false;
    },

    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
      state.isLoading = false;
    },

    clearError: (state) => {
      state.error = null;
    },
  },
});

export const {
  setLoading,
  setCredentials,
  setTokens,
  setError,
  clearAuth,
  clearError,
} = authSlice.actions;

export default authSlice.reducer;

// Selectors
export const selectAuth = (state: RootState) => state.account.auth;
export const selectToken = (state: RootState) => state.account.auth.token;
export const selectIsAuthenticated = (state: RootState) =>
  state.account.auth.isAuthenticated;
export const selectAuthLoading = (state: RootState) =>
  state.account.auth.isLoading;
export const selectAuthError = (state: RootState) => state.account.auth.error;
