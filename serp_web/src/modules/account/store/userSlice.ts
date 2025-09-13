/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User profile state management
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { RootState } from '@/lib/store';
import type { User } from '../types';

interface UserState {
  profile: User | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: UserState = {
  profile: null,
  isLoading: false,
  error: null,
};

const userSlice = createSlice({
  name: 'account/user',
  initialState,
  reducers: {
    setProfile: (state, action: PayloadAction<User>) => {
      state.profile = action.payload;
      state.error = null;
    },

    updateProfile: (state, action: PayloadAction<Partial<User>>) => {
      if (state.profile) {
        state.profile = { ...state.profile, ...action.payload };
      }
    },

    setLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },

    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload;
      state.isLoading = false;
    },

    clearProfile: (state) => {
      state.profile = null;
      state.error = null;
      state.isLoading = false;
    },
  },
});

export const { setProfile, updateProfile, setLoading, setError, clearProfile } =
  userSlice.actions;

export default userSlice.reducer;

// Selectors
export const selectUserProfile = (state: RootState) =>
  state.account.user.profile;
export const selectUserLoading = (state: RootState) =>
  state.account.user.isLoading;
export const selectUserError = (state: RootState) => state.account.user.error;
