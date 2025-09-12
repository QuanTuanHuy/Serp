/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication business logic hooks
 */

import { useCallback, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useNotification } from '@/shared/hooks';
import { isSuccessResponse, getErrorMessage } from '@/lib/store/api';
import {
  useLoginMutation,
  useRegisterMutation,
  useRefreshTokenMutation,
  useRevokeTokenMutation,
  useGetCurrentUserQuery,
} from '../services/authApi';
import {
  setCredentials,
  setTokens,
  setUser,
  setError,
  clearAuth,
  setLoading,
  selectAuth,
  selectIsAuthenticated,
  selectUser,
  selectToken,
} from '../store/authSlice';
import type { LoginRequest, RegisterRequest, User } from '../types';

export const useAuth = () => {
  const dispatch = useAppDispatch();
  const notification = useNotification();
  const auth = useAppSelector(selectAuth);
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const user = useAppSelector(selectUser);
  const token = useAppSelector(selectToken);

  const [loginMutation] = useLoginMutation();
  const [registerMutation] = useRegisterMutation();
  const [refreshTokenMutation] = useRefreshTokenMutation();
  const [revokeTokenMutation] = useRevokeTokenMutation();

  const {
    data: currentUserData,
    isLoading: userLoading,
    refetch: refetchUser,
  } = useGetCurrentUserQuery(undefined, {
    skip: !isAuthenticated || !token,
  });

  // Update user data when query succeeds
  useEffect(() => {
    if (
      isSuccessResponse(currentUserData) &&
      currentUserData?.data &&
      isAuthenticated
    ) {
      dispatch(setUser(currentUserData.data));
    }
  }, [currentUserData, isAuthenticated, dispatch]);

  const login = useCallback(
    async (credentials: LoginRequest) => {
      try {
        dispatch(setLoading(true));
        const result = await loginMutation(credentials).unwrap();

        if (isSuccessResponse(result)) {
          dispatch(
            setTokens({
              token: result.data.accessToken,
              refreshToken: result.data.refreshToken,
            })
          );

          // Try to fetch user profile after login
          try {
            await refetchUser();
          } catch (userError) {
            console.warn(
              'Failed to fetch user profile after login:',
              userError
            );
            // Create a basic user object from token if profile fetch fails
            const mockUser: User = {
              id: 1,
              email: credentials.email,
              firstName: '',
              lastName: '',
              fullName: 'User',
              roles: ['USER'],
              isActive: true,
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
            dispatch(setUser(mockUser));
          }

          notification.success('Login successful!', {
            description: `Welcome back!`,
          });
          return { success: true, data: result.data };
        } else {
          throw new Error(result.message);
        }
      } catch (error: any) {
        const errorMessage = getErrorMessage(error);
        dispatch(setError(errorMessage));
        notification.error('Login failed', {
          description: errorMessage,
        });
        return { success: false, error: errorMessage };
      } finally {
        dispatch(setLoading(false));
      }
    },
    [loginMutation, dispatch, notification]
  );

  const register = useCallback(
    async (userData: RegisterRequest) => {
      try {
        dispatch(setLoading(true));
        const result = await registerMutation(userData).unwrap();

        if (isSuccessResponse(result)) {
          dispatch(
            setTokens({
              token: result.data.accessToken,
              refreshToken: result.data.refreshToken,
            })
          );

          // Try to fetch user profile after registration
          try {
            await refetchUser();
          } catch (userError) {
            console.warn(
              'Failed to fetch user profile after registration:',
              userError
            );
            // Create a basic user object if profile fetch fails
            const mockUser: User = {
              id: 1,
              email: userData.email,
              firstName: userData.firstName,
              lastName: userData.lastName,
              fullName: `${userData.firstName} ${userData.lastName}`,
              roles: ['USER'],
              isActive: true,
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
            dispatch(setUser(mockUser));
          }

          notification.success('Registration successful!', {
            description: `Welcome to SERP, ${userData.firstName} ${userData.lastName}!`,
          });
          return { success: true, data: result.data };
        } else {
          throw new Error(result.message);
        }
      } catch (error: any) {
        const errorMessage =
          error.data?.message || error.message || 'Registration failed';
        dispatch(setError(errorMessage));
        notification.error('Registration failed', {
          description: errorMessage,
        });
        return { success: false, error: errorMessage };
      } finally {
        dispatch(setLoading(false));
      }
    },
    [registerMutation, dispatch, notification]
  );

  const logout = useCallback(
    async (showNotification = true) => {
      try {
        if (auth.refreshToken) {
          await revokeTokenMutation({
            refreshToken: auth.refreshToken,
          }).unwrap();
        }
      } catch (error) {
        console.warn('Token revoke failed:', error);
      } finally {
        dispatch(clearAuth());
        if (showNotification) {
          notification.success('Logged out successfully');
        }
      }
    },
    [auth.refreshToken, revokeTokenMutation, dispatch, notification]
  );

  const refreshToken = useCallback(async () => {
    if (!auth.refreshToken) {
      dispatch(clearAuth());
      return false;
    }

    try {
      const result = await refreshTokenMutation({
        refreshToken: auth.refreshToken,
      }).unwrap();

      if (isSuccessResponse(result)) {
        dispatch(
          setTokens({
            token: result.data.accessToken,
            refreshToken: result.data.refreshToken,
          })
        );
        return true;
      } else {
        throw new Error(result.message);
      }
    } catch (error) {
      console.error('Token refresh failed:', error);
      dispatch(clearAuth());
      return false;
    }
  }, [auth.refreshToken, refreshTokenMutation, dispatch]);

  return {
    // State
    ...auth,
    isAuthenticated,
    user,
    token,
    isUserLoading: userLoading,

    // Actions
    login,
    register,
    logout,
    refreshToken,
    refetchUser,

    // Utilities
    clearError: () => dispatch(setError(null)),
  };
};
