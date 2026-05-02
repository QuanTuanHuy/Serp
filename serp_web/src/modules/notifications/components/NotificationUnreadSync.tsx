/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Sync Redux unread badge with REST unread-count
 */

'use client';

import { useEffect } from 'react';
import { useAuth } from '@/modules/account/hooks';
import { useAppDispatch } from '@/shared/hooks';
import { useGetUnreadCountQuery } from '../services/notificationApi';
import { syncUnreadTotalFromApi } from '../store/notificationSlice';

export function NotificationUnreadSync() {
  const { isAuthenticated, token } = useAuth();
  const dispatch = useAppDispatch();

  const { data } = useGetUnreadCountQuery(undefined, {
    skip: !isAuthenticated || !token,
    pollingInterval: 120_000,
    refetchOnFocus: true,
    refetchOnReconnect: true,
  });

  useEffect(() => {
    if (data && typeof data.unread_count === 'number') {
      dispatch(syncUnreadTotalFromApi(data.unread_count));
    }
  }, [data, dispatch]);

  return null;
}
