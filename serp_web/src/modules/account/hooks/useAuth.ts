/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Authentication business logic orchestration hook
 */

'use client';

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
  useLazyGetCurrentUserQuery,
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

function createFallbackUser({
  email,
  firstName = '',
  lastName = '',
  organizationName,
}: {
  email: string;
  firstName?: string;
  lastName?: string;
  organizationName?: string;
}): User {
  const fullName = [firstName, lastName].filter(Boolean).join(' ').trim();
  const timestamp = new Date().toISOString();

  return {
    id: 1,
    email,
    firstName,
    lastName,
    fullName: fullName || 'User',
    roles: ['USER'],
    organizationName,
    isActive: true,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

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
  const [fetchCurrentUser] = useLazyGetCurrentUserQuery();

  const {
    data: currentUserData,
    isLoading: userLoading,
    refetch: refetchUser,
  } = useGetCurrentUserQuery(undefined, {
    skip: !isAuthenticated || !token,
  });

  useEffect(() => {
    if (
      isSuccessResponse(currentUserData) &&
      currentUserData?.data &&
      isAuthenticated
    ) {
      dispatch(setProfile(currentUserData.data));
    }
  }, [currentUserData, isAuthenticated, dispatch]);

  const hydrateCurrentUser = useCallback(
    async (fallbackUser: User, actionLabel: 'login' | 'registration') => {
      try {
        const profileResponse = await fetchCurrentUser(undefined).unwrap();

        if (isSuccessResponse(profileResponse) && profileResponse.data) {
          dispatch(setProfile(profileResponse.data));
          return profileResponse.data;
        }

        throw new Error(
          profileResponse.message ||
            `Failed to load the user profile after ${actionLabel}.`
        );
      } catch (userError) {
        console.warn(
          `Failed to fetch user profile after ${actionLabel}:`,
          userError
        );
        dispatch(setProfile(fallbackUser));
        return fallbackUser;
      }
    },
    [dispatch, fetchCurrentUser]
  );

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

          await hydrateCurrentUser(
            createFallbackUser({ email: credentials.email }),
            'login'
          );

          notification.success('Login successful!', {
            description: 'Welcome back.',
          });

          router.replace('/home');

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
    [dispatch, hydrateCurrentUser, loginMutation, notification, router]
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

          await hydrateCurrentUser(
            createFallbackUser({
              email: userData.email,
              firstName: userData.firstName,
              lastName: userData.lastName,
              organizationName: userData.organization.name,
            }),
            'registration'
          );

          notification.success('Registration successful!', {
            description: `Welcome to SERP, ${userData.firstName} ${userData.lastName}.`,
          });

          router.replace('/home');

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
    [dispatch, hydrateCurrentUser, notification, registerMutation, router]
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

        router.replace('/auth');
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
  }, [auth.refreshToken, refreshTokenMutation, dispatch]);

  const clearErrorState = useCallback(() => {
    dispatch(setError(null));
  }, [dispatch]);

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
    clearError: clearErrorState,
  };
};
