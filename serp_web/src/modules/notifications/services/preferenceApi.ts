/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Notification preference API
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  PreferenceResponse,
  UpdatePreferenceRequest,
} from '../types/preference.types';

export const preferenceApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPreferences: builder.query<PreferenceResponse, void>({
      query: () => ({
        url: '/preferences',
        method: 'GET',
      }),
      extraOptions: { service: 'ns' },
      providesTags: [{ type: 'NotificationPreference', id: 'CURRENT' }],
      transformResponse: createDataTransform<PreferenceResponse>(),
    }),

    updatePreferences: builder.mutation<
      PreferenceResponse,
      UpdatePreferenceRequest
    >({
      query: (body) => ({
        url: '/preferences',
        method: 'PATCH',
        body,
      }),
      extraOptions: { service: 'ns' },
      invalidatesTags: [{ type: 'NotificationPreference', id: 'CURRENT' }],
      transformResponse: createDataTransform<PreferenceResponse>(),
    }),
  }),
  overrideExisting: false,
});

export const { useGetPreferencesQuery, useUpdatePreferencesMutation } =
  preferenceApi;
