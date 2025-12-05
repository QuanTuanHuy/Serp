/*
Author: QuanTuanHuy
Description: Part of Serp Project - Shipments management hook (read-only)
*/

'use client';

import { useMemo } from 'react';
import { useGetShipmentsByOrderIdQuery } from '../services/purchaseApi';

export function useShipments(orderId?: string) {
  // Queries
  const {
    data: shipmentsResponse,
    isLoading: isLoadingShipments,
    refetch: refetchShipments,
  } = useGetShipmentsByOrderIdQuery(orderId || '', {
    skip: !orderId,
  });

  const shipments = useMemo(
    () => shipmentsResponse?.data || [],
    [shipmentsResponse]
  );

  return {
    // Data
    shipments,
    isLoadingShipments,
    refetchShipments,
  };
}

export type UseShipmentsReturn = ReturnType<typeof useShipments>;
