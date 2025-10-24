/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - usePlans hook to manage subscription plans
 */

'use client';

import { useMemo, useCallback } from 'react';
import {
  useGetSubscriptionPlansQuery,
  useCreateSubscriptionPlanMutation,
  useUpdateSubscriptionPlanMutation,
  useDeleteSubscriptionPlanMutation,
} from '@/modules/admin/services/plans/plansApi';
import type { SubscriptionPlan } from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { useAppDispatch, useAppSelector } from '@/shared/hooks';
import {
  selectPlansViewMode,
  selectPlansDialogOpen,
  selectSelectedPlanId,
  setViewMode,
  setDialogOpen,
  setSelectedPlanId,
  clearSelectedPlan,
} from '@/modules/admin/store';
import { getErrorMessage } from '@/lib/store/api';

type CreateUpdatePayload = Omit<
  SubscriptionPlan,
  'id' | 'createdAt' | 'updatedAt' | 'createdBy' | 'updatedBy'
>;

export function usePlans() {
  const dispatch = useAppDispatch();
  const notification = useNotification();

  const viewMode = useAppSelector(selectPlansViewMode);
  const isDialogOpen = useAppSelector(selectPlansDialogOpen);
  const selectedPlanId = useAppSelector(selectSelectedPlanId);

  const {
    data: plans = [],
    isLoading,
    error,
    refetch,
  } = useGetSubscriptionPlansQuery();

  const [createPlanMutation, { isLoading: isCreating }] =
    useCreateSubscriptionPlanMutation();
  const [updatePlanMutation] = useUpdateSubscriptionPlanMutation();
  const [deletePlanMutation] = useDeleteSubscriptionPlanMutation();

  const selectedPlan = useMemo(
    () => plans.find((p) => p.id === selectedPlanId),
    [plans, selectedPlanId]
  );

  const stats = useMemo(
    () => ({
      total: plans.length,
      active: plans.filter((p) => p.isActive).length,
      custom: plans.filter((p) => p.isCustom).length,
    }),
    [plans]
  );

  const openCreateDialog = useCallback(() => {
    dispatch(setSelectedPlanId(null));
    dispatch(setDialogOpen(true));
  }, [dispatch]);

  const openEditDialog = useCallback(
    (plan: SubscriptionPlan) => {
      dispatch(setSelectedPlanId(plan.id));
      dispatch(setDialogOpen(true));
    },
    [dispatch]
  );

  const closeDialog = useCallback(() => {
    dispatch(setDialogOpen(false));
    dispatch(clearSelectedPlan());
  }, [dispatch]);

  const setPlansViewMode = useCallback(
    (mode: 'grid' | 'table') => {
      dispatch(setViewMode(mode));
    },
    [dispatch]
  );

  const createPlan = useCallback(
    async (data: CreateUpdatePayload) => {
      try {
        await createPlanMutation(data).unwrap();
        notification.success('Plan created successfully');
        closeDialog();
      } catch (err: any) {
        notification.error(getErrorMessage(err));
        throw err;
      }
    },
    [createPlanMutation, notification, closeDialog]
  );

  const updatePlan = useCallback(
    async (id: number | string, data: Partial<SubscriptionPlan>) => {
      try {
        await updatePlanMutation({ id: String(id), data }).unwrap();
        notification.success('Plan updated successfully');
        closeDialog();
      } catch (err: any) {
        notification.error(getErrorMessage(err));
        throw err;
      }
    },
    [updatePlanMutation, notification, closeDialog]
  );

  const toggleActive = useCallback(
    async (id: number | string, isActive: boolean) => {
      try {
        await updatePlanMutation({
          id: String(id),
          data: { isActive: !isActive },
        }).unwrap();
        notification.success(
          `Plan ${isActive ? 'deactivated' : 'activated'} successfully`
        );
      } catch (err: any) {
        notification.error(getErrorMessage(err));
      }
    },
    [updatePlanMutation, notification]
  );

  const deletePlan = useCallback(
    async (id: number | string) => {
      try {
        await deletePlanMutation(String(id)).unwrap();
        notification.success('Plan deleted successfully');
      } catch (err: any) {
        notification.error(getErrorMessage(err));
      }
    },
    [deletePlanMutation, notification]
  );

  const submitPlan = useCallback(
    async (data: any) => {
      if (selectedPlan) {
        return updatePlan(selectedPlan.id, data);
      }
      return createPlan(data);
    },
    [selectedPlan, updatePlan, createPlan]
  );

  return {
    // data
    plans,
    stats,
    selectedPlan,
    // state
    viewMode,
    isDialogOpen,
    isCreating,
    isLoading,
    error,
    // actions
    refetch,
    openCreateDialog,
    openEditDialog,
    closeDialog,
    setPlansViewMode,
    submitPlan,
    createPlan,
    updatePlan,
    deletePlan,
    toggleActive,
  };
}

export type UsePlansReturn = ReturnType<typeof usePlans>;
