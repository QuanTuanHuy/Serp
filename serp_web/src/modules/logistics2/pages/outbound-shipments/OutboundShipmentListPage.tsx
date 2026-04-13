/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics2 outbound shipments list page
*/

'use client';

import { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/store';
import { useGetOutboundShipmentsQuery } from '../../api/logistics2Api';
import {
  setActiveModule,
  setOutboundShipmentFilters,
  setOutboundShipmentPagination,
} from '../../store';
import {
  selectOutboundShipmentFilters,
  selectOutboundShipmentPagination,
} from '../../store/selectors';
import { EntityListScaffold } from '../common/EntityListScaffold';

export const OutboundShipmentListPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const filters = useAppSelector(selectOutboundShipmentFilters);
  const pagination = useAppSelector(selectOutboundShipmentPagination);
  const [searchValue, setSearchValue] = useState(filters.query || '');

  useEffect(() => {
    dispatch(setActiveModule('outbound-shipments'));
  }, [dispatch]);

  const { data, isLoading, error, refetch } = useGetOutboundShipmentsQuery({
    filters,
    pagination,
  });

  const items = data?.data?.items || [];
  const totalItems = data?.data?.totalItems || 0;
  const totalPages = data?.data?.totalPages || 0;
  const currentPage = data?.data?.currentPage ?? pagination.page ?? 0;

  const handleSearch = () => {
    dispatch(
      setOutboundShipmentFilters({
        ...filters,
        query: searchValue || undefined,
      })
    );
    dispatch(setOutboundShipmentPagination({ ...pagination, page: 0 }));
  };

  const handleReset = () => {
    setSearchValue('');
    dispatch(setOutboundShipmentFilters({}));
    dispatch(setOutboundShipmentPagination({ ...pagination, page: 0 }));
  };

  return (
    <EntityListScaffold
      title='Outbound Shipments'
      description='Monitor outbound shipment documents and statuses'
      searchValue={searchValue}
      searchPlaceholder='Search by shipment name, order ID...'
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
          setOutboundShipmentPagination({
            ...pagination,
            page: currentPage - 1,
          })
        );
      }}
      onNextPage={() => {
        if (totalPages <= 1 || currentPage >= totalPages - 1) return;
        dispatch(
          setOutboundShipmentPagination({
            ...pagination,
            page: currentPage + 1,
          })
        );
      }}
      emptyMessage='No outbound shipments matched your filters'
    />
  );
};
