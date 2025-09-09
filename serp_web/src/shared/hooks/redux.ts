/**
 * Redux Typed Hooks
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from '@/lib/store';

// Use throughout your app instead of plain `useDispatch` and `useSelector`
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector = <T>(selector: (state: RootState) => T): T =>
  useSelector(selector);

// Custom hook for getting loading states from RTK Query
export const useAppLoading = () => {
  return useAppSelector((state) => {
    // Check if any API call is pending
    const apiState = (state as any).api;
    if (!apiState) return false;

    const apiQueries = apiState.queries;
    const apiMutations = apiState.mutations;

    const isQueryLoading = Object.values(apiQueries).some(
      (query: any) => query?.status === 'pending'
    );

    const isMutationLoading = Object.values(apiMutations).some(
      (mutation: any) => mutation?.status === 'pending'
    );

    return isQueryLoading || isMutationLoading;
  });
};

// Custom hook for getting error states from RTK Query
export const useAppErrors = () => {
  return useAppSelector((state) => {
    const apiState = (state as any).api;
    if (!apiState) return [];

    const apiQueries = apiState.queries;
    const apiMutations = apiState.mutations;

    const queryErrors = Object.values(apiQueries)
      .filter((query: any) => query?.error)
      .map((query: any) => query.error);

    const mutationErrors = Object.values(apiMutations)
      .filter((mutation: any) => mutation?.error)
      .map((mutation: any) => mutation.error);

    return [...queryErrors, ...mutationErrors];
  });
};
