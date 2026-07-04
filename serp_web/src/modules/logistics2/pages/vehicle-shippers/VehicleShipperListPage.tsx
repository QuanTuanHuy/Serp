'use client';

import { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { useGetVehicleShippersQuery } from '../../api/logistics2Api';
import {
  setActiveModule,
  setVehicleShipperFilters,
  setVehicleShipperPagination,
} from '../../store';
import {
  selectVehicleShipperFilters,
  selectVehicleShipperPagination,
} from '../../store/selectors';
import { EntityListScaffold } from '../common/EntityListScaffold';

export const VehicleShipperListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const filters = useAppSelector(selectVehicleShipperFilters);
  const pagination = useAppSelector(selectVehicleShipperPagination);
  const [vehicleIdValue, setVehicleIdValue] = useState(filters.vehicleId || '');

  useEffect(() => {
    dispatch(setActiveModule('vehicle-shippers'));
  }, [dispatch]);

  const { data, isLoading, error, refetch } = useGetVehicleShippersQuery({
    filters,
    pagination,
  });

  const items = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage ?? pagination.page ?? 0;

  const handleSearch = () => {
    dispatch(
      setVehicleShipperFilters({
        ...filters,
        vehicleId: vehicleIdValue || undefined,
      })
    );
    dispatch(setVehicleShipperPagination({ ...pagination, page: 0 }));
  };

  const handleReset = () => {
    setVehicleIdValue('');
    dispatch(setVehicleShipperFilters({}));
    dispatch(setVehicleShipperPagination({ ...pagination, page: 0 }));
  };

  return (
    <EntityListScaffold
      title='Vehicle Shipper Assignments'
      description='Monitor vehicle-to-shipper assignments by working date'
      searchValue={vehicleIdValue}
      searchPlaceholder='Filter by vehicle ID'
      onSearchValueChange={setVehicleIdValue}
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
          setVehicleShipperPagination({ ...pagination, page: currentPage - 1 })
        );
      }}
      onNextPage={() => {
        if (totalPages <= 1 || currentPage >= totalPages - 1) return;
        dispatch(
          setVehicleShipperPagination({ ...pagination, page: currentPage + 1 })
        );
      }}
      emptyMessage='No assignments matched your filters'
    />
  );
};
