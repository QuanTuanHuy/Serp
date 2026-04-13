/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 vehicles list page
*/

'use client';

import { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { useGetVehiclesQuery } from '../../api/logistics2Api';
import {
  setActiveModule,
  setVehicleFilters,
  setVehiclePagination,
} from '../../store';
import {
  selectVehicleFilters,
  selectVehiclePagination,
} from '../../store/selectors';
import { EntityListScaffold } from '../common/EntityListScaffold';

export const VehicleListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const filters = useAppSelector(selectVehicleFilters);
  const pagination = useAppSelector(selectVehiclePagination);
  const [searchValue, setSearchValue] = useState(filters.query || '');

  useEffect(() => {
    dispatch(setActiveModule('vehicles'));
  }, [dispatch]);

  const { data, isLoading, error, refetch } = useGetVehiclesQuery({
    filters,
    pagination,
  });

  const items = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage ?? pagination.page ?? 0;

  const handleSearch = () => {
    dispatch(
      setVehicleFilters({
        ...filters,
        query: searchValue || undefined,
      })
    );
    dispatch(setVehiclePagination({ ...pagination, page: 0 }));
  };

  const handleReset = () => {
    setSearchValue('');
    dispatch(setVehicleFilters({}));
    dispatch(setVehiclePagination({ ...pagination, page: 0 }));
  };

  return (
    <EntityListScaffold
      title='Vehicles'
      description='Manage transport capacity and operating status'
      searchValue={searchValue}
      searchPlaceholder='Search by plate number or type'
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
          setVehiclePagination({ ...pagination, page: currentPage - 1 })
        );
      }}
      onNextPage={() => {
        if (totalPages <= 1 || currentPage >= totalPages - 1) return;
        dispatch(
          setVehiclePagination({ ...pagination, page: currentPage + 1 })
        );
      }}
      emptyMessage='No vehicles matched your filters'
    />
  );
};
