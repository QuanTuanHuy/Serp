/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - usePlans hook to manage subscription plans
 */

'use client';

import { useMemo, useCallback, useState } from 'react';
import {
  useGetSubscriptionPlansWithPaginationQuery,
  useCreateSubscriptionPlanMutation,
  useUpdateSubscriptionPlanMutation,
  useDeleteSubscriptionPlanMutation,
  useGetPlanModulesQuery,
  useAddModuleToPlanMutation,
  useRemoveModuleFromPlanMutation,
  GetPlansParams,
} from '@/modules/admin/services/plans/plansApi';
import type {
  SubscriptionPlan,
  AddModuleToPlanRequest,
} from '@/modules/admin/types';
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

export interface PlansFilters {
  organizationId?: number;
  isCustom?: boolean;
  isActive?: boolean;
  search?: string;
}

export function usePlans() {
  const dispatch = useAppDispatch();
  const notification = useNotification();

  const viewMode = useAppSelector(selectPlansViewMode);
  const isDialogOpen = useAppSelector(selectPlansDialogOpen);
  const selectedPlanId = useAppSelector(selectSelectedPlanId);

  // Filters and pagination state
  const [filters, setFilters] = useState<PlansFilters>({});
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // State for modules dialog
  const [modulesDialogOpen, setModulesDialogOpen] = useState(false);
  const [modulesDialogPlanId, setModulesDialogPlanId] = useState<string | null>(
    null
  );

  const queryParams: GetPlansParams = useMemo(
    () => ({
      page: currentPage,
      pageSize,
      ...filters,
    }),
    [currentPage, filters]
  );

  const {
    data: plansResponse,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetSubscriptionPlansWithPaginationQuery(queryParams);

  const plans = useMemo(
    () => plansResponse?.data?.items || [],
    [plansResponse]
  );
  const pagination = useMemo(
    () => ({
      currentPage: plansResponse?.data?.currentPage || 0,
      totalPages: plansResponse?.data?.totalPages || 0,
      totalItems: plansResponse?.data?.totalItems || 0,
    }),
    [plansResponse]
  );

  const [createPlanMutation, { isLoading: isCreating }] =
    useCreateSubscriptionPlanMutation();
  const [updatePlanMutation] = useUpdateSubscriptionPlanMutation();
  const [deletePlanMutation] = useDeleteSubscriptionPlanMutation();

  // Plan modules queries
  const { data: planModules = [], isLoading: isLoadingModules } =
    useGetPlanModulesQuery(modulesDialogPlanId || '', {
      skip: !modulesDialogPlanId,
    });

  const [addModuleToPlanMutation, { isLoading: isAddingModule }] =
    useAddModuleToPlanMutation();
  const [removeModuleFromPlanMutation, { isLoading: isRemovingModule }] =
    useRemoveModuleFromPlanMutation();

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
    async (data: Partial<SubscriptionPlan>) => {
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

  const handleFilterChange = useCallback(
    (key: keyof PlansFilters, value: any) => {
      setFilters((prev) => ({ ...prev, [key]: value }));
      setCurrentPage(0);
    },
    []
  );

  const handlePageChange = useCallback((page: number) => {
    setCurrentPage(page);
  }, []);

  // Module management functions
  const openModulesDialog = useCallback((planId: string | number) => {
    setModulesDialogPlanId(String(planId));
    setModulesDialogOpen(true);
  }, []);

  const closeModulesDialog = useCallback(() => {
    setModulesDialogOpen(false);
    setModulesDialogPlanId(null);
  }, []);

  const addModuleToPlan = useCallback(
    async (planId: string, data: AddModuleToPlanRequest) => {
      try {
        await addModuleToPlanMutation({ planId, data }).unwrap();
        notification.success('Module added to plan successfully');
      } catch (err: any) {
        notification.error(getErrorMessage(err));
        throw err;
      }
    },
    [addModuleToPlanMutation, notification]
  );

  const removeModuleFromPlan = useCallback(
    async (planId: string, moduleId: number) => {
      try {
        await removeModuleFromPlanMutation({ planId, moduleId }).unwrap();
        notification.success('Module removed from plan successfully');
      } catch (err: any) {
        notification.error(getErrorMessage(err));
        throw err;
      }
    },
    [removeModuleFromPlanMutation, notification]
  );

  return {
    // data
    plans,
    stats,
    selectedPlan,
    planModules,
    pagination,
    filters,
    // state
    viewMode,
    isDialogOpen,
    modulesDialogOpen,
    modulesDialogPlanId,
    isCreating,
    isLoading,
    isFetching,
    isLoadingModules,
    isAddingModule,
    isRemovingModule,
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
    handleFilterChange,
    handlePageChange,
    // module actions
    openModulesDialog,
    closeModulesDialog,
    addModuleToPlan,
    removeModuleFromPlan,
  };
}

export type UsePlansReturn = ReturnType<typeof usePlans>;
