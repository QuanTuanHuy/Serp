/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 delivery plans list page
*/

'use client';

import { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { useGetDeliveryPlansQuery } from '../../api/logistics2Api';
import {
  setActiveModule,
  setDeliveryPlanFilters,
  setDeliveryPlanPagination,
} from '../../store';
import {
  selectDeliveryPlanFilters,
  selectDeliveryPlanPagination,
} from '../../store/selectors';
import { EntityListScaffold } from '../common/EntityListScaffold';

export const DeliveryPlanListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const filters = useAppSelector(selectDeliveryPlanFilters);
  const pagination = useAppSelector(selectDeliveryPlanPagination);
  const [searchValue, setSearchValue] = useState(filters.query || '');

  useEffect(() => {
    dispatch(setActiveModule('delivery-plans'));
  }, [dispatch]);

  const { data, isLoading, error, refetch } = useGetDeliveryPlansQuery({
    filters,
    pagination,
  });

  const items = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage ?? pagination.page ?? 0;

  const handleSearch = () => {
    dispatch(
      setDeliveryPlanFilters({
        ...filters,
        query: searchValue || undefined,
      })
    );
    dispatch(setDeliveryPlanPagination({ ...pagination, page: 0 }));
  };

  const handleReset = () => {
    setSearchValue('');
    dispatch(setDeliveryPlanFilters({}));
    dispatch(setDeliveryPlanPagination({ ...pagination, page: 0 }));
  };

  return (
    <EntityListScaffold
      title='Delivery Plans'
      description='Search and manage outbound delivery plans'
      searchValue={searchValue}
      searchPlaceholder='Search by plan code, facility, or keyword'
      onSearchValueChange={setSearchValue}
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
        dispatch(
          setDeliveryPlanPagination({ ...pagination, page: currentPage - 1 })
        );
      }}
      onNextPage={() => {
        if (totalPages <= 1 || currentPage >= totalPages - 1) return;
        dispatch(
          setDeliveryPlanPagination({ ...pagination, page: currentPage + 1 })
        );
      }}
      emptyMessage='No delivery plans matched your filters'
    />
  );
};
