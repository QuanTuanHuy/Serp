/**
 * Auth Store Barrel Exports
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication store exports
 */

export { default as authSlice } from './authSlice';
export {
  setLoading,
  setCredentials,
  setTokens,
  setError,
  clearAuth,
  clearError,
  selectAuth,
  selectToken,
  selectIsAuthenticated,
  selectAuthLoading,
  selectAuthError,
} from './authSlice';

export { default as userSlice } from './userSlice';
export {
  setProfile,
  updateProfile,
  setLoading as setUserLoading,
  setError as setUserError,
  clearProfile,
  selectUserProfile,
  selectUserLoading,
  selectUserError,
} from './userSlice';
