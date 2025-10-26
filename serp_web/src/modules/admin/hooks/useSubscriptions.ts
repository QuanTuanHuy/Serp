/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - useSubscriptions hook for Subscriptions page
 */

'use client';

import { useCallback, useMemo } from 'react';
import {
  useGetSubscriptionsQuery,
  useActivateSubscriptionMutation,
  useRejectSubscriptionMutation,
  useExpireSubscriptionMutation,
} from '@/modules/admin';
import type {
  OrganizationSubscription,
  SubscriptionFilters,
} from '@/modules/admin/types';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage as getApiErrorMessage } from '@/lib/store/api/utils';
import {
  selectSubscriptionsFilters,
  setSubscriptionsFilters,
  setSubscriptionsStatus,
  setSubscriptionsOrganizationId,
  setSubscriptionsPlanId,
  setSubscriptionsBillingCycle,
  setSubscriptionsPage,
  setSubscriptionsPageSize,
  setSubscriptionsSort,
} from '@/modules/admin/store';

export function useSubscriptions() {
  const dispatch = useAppDispatch();
  const notification = useNotification();

  const filters = useAppSelector(selectSubscriptionsFilters);

  const {
    data: response,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetSubscriptionsQuery(filters);

  const [activateSubscription, { isLoading: isActivating }] =
    useActivateSubscriptionMutation();
  const [rejectSubscription, { isLoading: isRejecting }] =
    useRejectSubscriptionMutation();
  const [expireSubscription, { isLoading: isExpiring }] =
    useExpireSubscriptionMutation();

  const subscriptions: OrganizationSubscription[] = useMemo(
    () => response?.data.items || [],
    [response]
  );

  const pagination = useMemo(
    () => ({
      totalPages: response?.data.totalPages || 0,
      currentPage: response?.data.currentPage || 0,
      totalItems: response?.data.totalItems || 0,
    }),
    [response]
  );

  const handleFilterChange = useCallback(
    (key: keyof SubscriptionFilters, value: any) => {
      switch (key) {
        case 'status':
          dispatch(setSubscriptionsStatus(value || undefined));
          break;
        case 'organizationId':
          dispatch(setSubscriptionsOrganizationId(value || undefined));
          break;
        case 'planId':
          dispatch(setSubscriptionsPlanId(value || undefined));
          break;
        case 'billingCycle':
          dispatch(setSubscriptionsBillingCycle(value || undefined));
          break;
        case 'page':
          dispatch(setSubscriptionsPage(value as number));
          break;
        case 'pageSize':
          dispatch(setSubscriptionsPageSize(value as number));
          break;
        case 'sortBy':
        case 'sortDir':
          dispatch(
            setSubscriptionsSort({
              sortBy: (key === 'sortBy' ? value : filters.sortBy) as string,
              sortDir: (key === 'sortDir' ? value : filters.sortDir) as
                | 'ASC'
                | 'DESC',
            })
          );
          break;
        default:
          dispatch(setSubscriptionsFilters({ [key]: value } as any));
      }
    },
    [dispatch, filters.sortBy, filters.sortDir]
  );

  const handlePageChange = useCallback(
    (newPage: number) => dispatch(setSubscriptionsPage(newPage)),
    [dispatch]
  );

  const handleActivate = useCallback(
    async (subscriptionId: number) => {
      try {
        await activateSubscription({ subscriptionId }).unwrap();
        notification.success('Subscription activated');
        refetch();
        return true;
      } catch (err) {
        notification.error(getApiErrorMessage(err));
        return false;
      }
    },
    [activateSubscription, refetch]
  );

  const handleReject = useCallback(
    async (subscriptionId: number, reason: string) => {
      try {
        await rejectSubscription({ subscriptionId, reason }).unwrap();
        notification.success('Subscription rejected');
        refetch();
        return true;
      } catch (err) {
        notification.error(getApiErrorMessage(err));
        return false;
      }
    },
    [rejectSubscription, refetch]
  );

  const handleExpire = useCallback(
    async (subscriptionId: number) => {
      try {
        await expireSubscription({ subscriptionId }).unwrap();
        notification.success('Subscription expired');
        refetch();
        return true;
      } catch (err) {
        notification.error(getApiErrorMessage(err));
        return false;
      }
    },
    [expireSubscription, refetch]
  );

  return {
    filters,
    subscriptions,
    pagination,
    isLoading,
    isFetching,
    isActivating,
    isRejecting,
    isExpiring,
    error,
    refetch,
    handleFilterChange,
    handlePageChange,
    handleActivate,
    handleReject,
    handleExpire,
  };
}

export type UseSubscriptionsReturn = ReturnType<typeof useSubscriptions>;
