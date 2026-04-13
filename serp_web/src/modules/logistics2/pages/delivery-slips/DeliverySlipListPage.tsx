/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 delivery slips list page
*/

'use client';

import { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { useGetDeliverySlipsQuery } from '../../api/logistics2Api';
import {
  setActiveModule,
  setDeliverySlipFilters,
  setDeliverySlipPagination,
} from '../../store';
import {
  selectDeliverySlipFilters,
  selectDeliverySlipPagination,
} from '../../store/selectors';
import { EntityListScaffold } from '../common/EntityListScaffold';

export const DeliverySlipListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const filters = useAppSelector(selectDeliverySlipFilters);
  const pagination = useAppSelector(selectDeliverySlipPagination);
  const [searchValue, setSearchValue] = useState(filters.query || '');

  useEffect(() => {
    dispatch(setActiveModule('delivery-slips'));
  }, [dispatch]);

  const { data, isLoading, error, refetch } = useGetDeliverySlipsQuery({
    filters,
    pagination,
  });

  const items = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage ?? pagination.page ?? 0;

  const handleSearch = () => {
    dispatch(
      setDeliverySlipFilters({
        ...filters,
        query: searchValue || undefined,
      })
    );
    dispatch(setDeliverySlipPagination({ ...pagination, page: 0 }));
  };

  const handleReset = () => {
    setSearchValue('');
    dispatch(setDeliverySlipFilters({}));
    dispatch(setDeliverySlipPagination({ ...pagination, page: 0 }));
  };

  return (
    <EntityListScaffold
      title='Delivery Slips'
      description='Track delivery slips and item-level execution'
      searchValue={searchValue}
      searchPlaceholder='Search by slip code, customer, facility...'
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
          setDeliverySlipPagination({ ...pagination, page: currentPage - 1 })
        );
      }}
      onNextPage={() => {
        if (totalPages <= 1 || currentPage >= totalPages - 1) return;
        dispatch(
          setDeliverySlipPagination({ ...pagination, page: currentPage + 1 })
        );
      }}
      emptyMessage='No delivery slips matched your filters'
    />
  );
};
