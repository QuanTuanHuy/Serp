/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication business logic orchestration hook
 */

import { useCallback, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useNotification } from '@/shared/hooks';
import { isSuccessResponse, getErrorMessage } from '@/lib/store';
import {
  useLoginMutation,
  useRegisterMutation,
  useRefreshTokenMutation,
  useRevokeTokenMutation,
  useGetCurrentUserQuery,
} from '../services';
import {
  setTokens,
  setError,
  clearAuth,
  setLoading,
  selectAuth,
  selectIsAuthenticated,
  selectToken,
  selectUserProfile,
  setProfile,
  clearProfile,
} from '../store';
import type { LoginRequest, RegisterRequest, User } from '../types';

export const useAuth = () => {
  const dispatch = useAppDispatch();
  const notification = useNotification();
  const router = useRouter();

  // Auth state selectors
  const auth = useAppSelector(selectAuth);
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const token = useAppSelector(selectToken);

  // User profile state selector
  const user = useAppSelector(selectUserProfile);

  // Mutations
  const [loginMutation] = useLoginMutation();
  const [registerMutation] = useRegisterMutation();
  const [refreshTokenMutation] = useRefreshTokenMutation();
  const [revokeTokenMutation] = useRevokeTokenMutation();

  // Current user query
  const {
    data: currentUserData,
    isLoading: userLoading,
    refetch: refetchUser,
  } = useGetCurrentUserQuery(undefined, {
    skip: !isAuthenticated || !token,
  });

  // Sync user data to user slice when query succeeds
  useEffect(() => {
    if (
      isSuccessResponse(currentUserData) &&
      currentUserData?.data &&
      isAuthenticated
    ) {
      dispatch(setProfile(currentUserData.data));
    }
  }, [currentUserData, isAuthenticated, dispatch]);

  // === AUTHENTICATION ACTIONS ===

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
            dispatch(setProfile(mockUser));
          }

          notification.success('Login successful!', {
            description: `Welcome back!`,
          });

          // Redirect to home page after successful login
          router.push('/home');

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
    [loginMutation, dispatch, notification, refetchUser, router]
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
            dispatch(setProfile(mockUser));
          }

          notification.success('Registration successful!', {
            description: `Welcome to SERP, ${userData.firstName} ${userData.lastName}!`,
          });

          // Redirect to home page after successful registration
          router.push('/home');

          return { success: true, data: result.data };
        } else {
          throw new Error(result.message);
        }
      } catch (error: any) {
        const errorMessage = getErrorMessage(error);
        dispatch(setError(errorMessage));
        notification.error('Registration failed', {
          description: errorMessage,
        });
        return { success: false, error: errorMessage };
      } finally {
        dispatch(setLoading(false));
      }
    },
    [registerMutation, dispatch, notification, refetchUser, router]
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
        // Clear both auth and user slices
        dispatch(clearAuth());
        dispatch(clearProfile());

        if (showNotification) {
          notification.success('Logged out successfully');
        }

        // Redirect to landing page after logout
        router.push('/');
      }
    },
    [auth.refreshToken, revokeTokenMutation, dispatch, notification, router]
  );

  const refreshToken = useCallback(async () => {
    if (!auth.refreshToken) {
      dispatch(clearAuth());
      dispatch(clearProfile());
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
      dispatch(clearProfile());
      return false;
    }
  }, [auth.refreshToken, refreshTokenMutation, dispatch, router]);

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
