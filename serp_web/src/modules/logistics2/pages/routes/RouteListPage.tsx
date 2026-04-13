/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 routes list page
*/

'use client';

import { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { useGetRoutesQuery } from '../../api/logistics2Api';
import {
  setActiveModule,
  setRouteFilters,
  setRoutePagination,
} from '../../store';
import {
  selectRouteFilters,
  selectRoutePagination,
} from '../../store/selectors';
import { EntityListScaffold } from '../common/EntityListScaffold';

export const RouteListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const filters = useAppSelector(selectRouteFilters);
  const pagination = useAppSelector(selectRoutePagination);
  const [deliveryPlanIdValue, setDeliveryPlanIdValue] = useState(
    filters.deliveryPlanId || ''
  );

  useEffect(() => {
    dispatch(setActiveModule('routes'));
  }, [dispatch]);

  const { data, isLoading, error, refetch } = useGetRoutesQuery({
    filters,
    pagination,
  });

  const items = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage ?? pagination.page ?? 0;

  const handleSearch = () => {
    dispatch(
      setRouteFilters({
        ...filters,
        deliveryPlanId: deliveryPlanIdValue || undefined,
      })
    );
    dispatch(setRoutePagination({ ...pagination, page: 0 }));
  };

  const handleReset = () => {
    setDeliveryPlanIdValue('');
    dispatch(setRouteFilters({}));
    dispatch(setRoutePagination({ ...pagination, page: 0 }));
  };

  return (
    <EntityListScaffold
      title='Routes'
      description='Review route assignments and stop execution status'
      searchValue={deliveryPlanIdValue}
      searchPlaceholder='Filter by delivery plan ID'
      onSearchValueChange={setDeliveryPlanIdValue}
      onSearch={handleSearch}
      onReset={handleReset}
      onRefresh={refetch}
      isLoading={isLoading}
      error={error}
      items={items}
      totalItems={totalItems}
      currentPage={currentPage}
      totalPages={totalPages}
      onPreviousPage={() => {
        if (currentPage <= 0) return;
        dispatch(setRoutePagination({ ...pagination, page: currentPage - 1 }));
      }}
      onNextPage={() => {
        if (totalPages <= 1 || currentPage >= totalPages - 1) return;
        dispatch(setRoutePagination({ ...pagination, page: currentPage + 1 }));
      }}
      emptyMessage='No routes matched your filters'
    />
  );
};
