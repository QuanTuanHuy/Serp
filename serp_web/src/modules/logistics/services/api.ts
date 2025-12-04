/**
 * Logistics Module - API Base Configuration
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - Custom API instance for logistics services
 * Logistics routes are structured as /logistics/api/v1/* (logistics comes BEFORE api/v1)
 */

import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type { RootState } from '@/lib/store/store';

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// Custom base query for logistics - no /api/v1 prefix
// Logistics backend uses /logistics/api/v1/* pattern (different from other modules)
const logisticsBaseQuery = fetchBaseQuery({
  baseUrl: API_BASE_URL, // No /api/v1 prefix
  prepareHeaders: (headers, { getState }) => {
    const state = getState() as RootState;
    const token = state.account.auth?.token;

    if (token) {
      headers.set('authorization', `Bearer ${token}`);
    }
    headers.set('content-type', 'application/json');

    return headers;
  },
});

export const logisticsApi = createApi({
  reducerPath: 'logisticsApi',
  baseQuery: logisticsBaseQuery,
  tagTypes: [
    'logistics/Address',
    'logistics/Category',
    'logistics/Customer',
    'logistics/Facility',
    'logistics/InventoryItem',
    'logistics/Order',
    'logistics/Product',
    'logistics/Shipment',
    'logistics/Supplier',
  ],
  endpoints: () => ({}),
});
