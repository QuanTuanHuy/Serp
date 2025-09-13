/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - User management business logic hook
 */

import { useCallback, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useNotification } from '@/shared/hooks';
import { selectIsAuthenticated } from '../store';
import {
  selectUserProfile,
  selectUserLoading,
  selectUserError,
  setProfile,
  setLoading as setUserLoading,
  setError as setUserError,
  clearProfile,
} from '../store';
import { useGetCurrentUserQuery } from '../services';
import { isSuccessResponse, getErrorMessage } from '@/lib/store';

export const useUser = () => {
  const dispatch = useAppDispatch();
  const notification = useNotification();

  // State selectors
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const user = useAppSelector(selectUserProfile);
  const loading = useAppSelector(selectUserLoading);
  const error = useAppSelector(selectUserError);

  // API hooks
  const {
    data: profileData,
    isLoading: profileLoading,
    error: profileApiError,
    refetch: refetchProfile,
  } = useGetCurrentUserQuery(undefined, {
    skip: !isAuthenticated || !user?.id,
    refetchOnMountOrArgChange: 300,
  });

  // Sync profile data from API to slice
  useEffect(() => {
    if (profileData && isSuccessResponse(profileData) && profileData.data) {
      dispatch(setProfile(profileData.data));
    }
  }, [profileData, dispatch]);

  // Handle API errors
  useEffect(() => {
    if (profileApiError) {
      const errorMessage = getErrorMessage(profileApiError);
      dispatch(setUserError(errorMessage));
    }
  }, [profileApiError, dispatch]);

  // Set loading states
  useEffect(() => {
    dispatch(setUserLoading(profileLoading));
  }, [profileLoading, dispatch]);

  // === PROFILE MANAGEMENT ===

  const refreshUserProfile = useCallback(async () => {
    try {
      await refetchProfile();
      return { success: true };
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      notification.error('Failed to refresh profile', {
        description: errorMessage,
      });
      return { success: false, error: errorMessage };
    }
  }, [refetchProfile, notification]);

  // === COMPUTED VALUES ===

  const getUserDisplayName = useCallback(() => {
    if (!user) return 'Unknown User';

    if (user.fullName) return user.fullName;
    if (user.firstName && user.lastName) {
      return `${user.firstName} ${user.lastName}`;
    }
    if (user.firstName) return user.firstName;
    if (user.email) return user.email;

    return 'Unknown User';
  }, [user]);

  const getUserInitials = useCallback(() => {
    if (!user) return 'U';

    if (user.firstName && user.lastName) {
      return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
    }
    if (user.fullName) {
      const names = user.fullName.split(' ');
      if (names.length >= 2) {
        return `${names[0][0]}${names[names.length - 1][0]}`.toUpperCase();
      }
      return user.fullName[0].toUpperCase();
    }
    if (user.email) {
      return user.email[0].toUpperCase();
    }

    return 'U';
  }, [user]);

  const clearUserProfile = useCallback(() => {
    dispatch(clearProfile());
  }, [dispatch]);

  return {
    // Core data
    user,
    isLoading: loading,
    error,

    // Profile actions
    refreshProfile: refreshUserProfile,
    clearProfile: clearUserProfile,

    // Utility functions
    getDisplayName: getUserDisplayName,
    getInitials: getUserInitials,

    // Refetch functions
    refetchProfile,
  };
};
